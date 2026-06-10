package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlLineEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis Count Index 쓰기 담당.
 *
 * <p>ordinal 산식: {@code (int)(blId * 2 + (MASTER ? 1 : 0))} — Redis 저장 없음.
 * <p>reverse-state 없음: 증분 diff는 호출측이 oldDocs를 OLTP에서 미리 조회해 전달.
 * <p>Redis 쓰기(비트맵 diff, flush) 위임: {@link PmsCountIndexRedisWriter}.
 * <p>벌크 빌드는 {@link PmsCountIndexBulkBuilder#rebuildFromMart()}를 사용한다.
 *
 * <p>meta 게이팅 규칙:
 * <ul>
 *   <li>빌드 시작: meta 삭제(not-ready)
 *   <li>빌드 완료: markComplete() — complete="1" + syncAt 기록
 *   <li>applyChanges 성공: syncAt만 갱신(complete 유지)
 *   <li>applyChanges 실패: markStale() — complete 필드 삭제 → not-ready
 * </ul>
 *
 * W1-A: deriveMembershipKeys dim 루프에서 jobDiv/bound 외 차원(cust/spc/liner/pol/pod/salesman/
 *        houseteam/salesclass/incoterms) 제거. blDocteamBitmap 제거.
 * W2: addLineKeys에 E3 composite 버킷 추가.
 *     일 복합({p}:ln:pd:{day}:c:{t}{s}:{TYPE}) = pd 보유 라인(실적일자 범위 쿼리용).
 *     전역 복합({p}:ln:c:{t}{s}:{TYPE}) = 라인 전수(무날짜 라인-술어 쿼리용). 2-bit(t/s).
 * W2-fix: 전역 복합을 pd 공백 라인 한정에서 모든 라인으로 확대 —
 *         ETD+documentTypes 등 무날짜 라인-술어 쿼리가 전역 복합을 조회하므로
 *         pd 보유 라인을 제외하면 거의 0으로 과소집계되던 결함 수정.
 * W3: addDocExistsKeys를 deriveMembershipKeys에 추가 —
 *     docs[]에서 status별 B/L-grain dcx:* 비트맵 키 파생.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart.count-index", name = "enabled", havingValue = "true")
public class PmsCountIndexMaintainer {

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final String prefix;
    private final PmsCountIndexDocSupport docSupport;
    private final PmsCountIndexRedisWriter redisWriter;

    /** overflow warn 1회 제한 플래그. */
    private volatile boolean blOverflowWarned = false;

    PmsCountIndexMaintainer(RedisTemplate<String, byte[]> redisTemplate, PmsMartProperties props) {
        this.redisTemplate = redisTemplate;
        this.prefix        = props.getCountIndex().getKeyPrefix();
        this.docSupport    = new PmsCountIndexDocSupport(redisTemplate, this.prefix);
        this.redisWriter   = new PmsCountIndexRedisWriter(redisTemplate, this.prefix);
    }

    // ── 공개 API ──────────────────────────────────────────────────────────────

    /**
     * 증분 diff 적용.
     *
     * oldDocs: 이번 변경 대상 B/L의 이전 Mart 문서(없으면 빈 List).
     * newDocs: 이번 변경 후 Mart 문서.
     *
     * 실패 시 markStale()을 호출해 isReady=false로 전환하고 warn.
     */
    public void applyChanges(List<PmsBlMartDocument> oldDocs, List<PmsBlMartDocument> newDocs) {
        if ((oldDocs == null || oldDocs.isEmpty()) && (newDocs == null || newDocs.isEmpty())) {
            return;
        }
        try {
            doApplyChanges(
                oldDocs != null ? oldDocs : List.of(),
                newDocs != null ? newDocs : List.of());
        } catch (Exception e) {
            log.warn("Count Index applyChanges 실패 — markStale: {}", e.toString());
            markStale();
        }
    }

    /**
     * 빌드 완료 표시. complete="1" + syncAt 기록.
     * PmsCountIndexBulkBuilder가 성공 완료 후 호출한다.
     */
    public void markComplete() {
        try {
            Map<String, byte[]> fields = new HashMap<>(4);
            fields.put(PmsCountIndexKeys.META_COMPLETE, "1".getBytes(StandardCharsets.UTF_8));
            fields.put(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            redisTemplate.opsForHash().putAll(PmsCountIndexKeys.meta(prefix), fields);
        } catch (Exception e) {
            log.warn("Count Index markComplete 실패: {}", e.toString());
        }
    }

    /**
     * 인덱스 stale 표시. complete 필드 삭제 → isReady=false.
     * applyChanges 실패 시 자동 호출.
     */
    public void markStale() {
        try {
            redisTemplate.opsForHash().delete(PmsCountIndexKeys.meta(prefix), PmsCountIndexKeys.META_COMPLETE);
        } catch (Exception e) {
            log.warn("Count Index markStale 실패: {}", e.toString());
        }
    }

    /**
     * prefix 하위 모든 키를 SCAN+UNLINK로 삭제한다(bulk rebuild 전 flush).
     */
    public void flushAll() {
        redisWriter.flushAll();
    }

    // ── 멤버십 파생 (순수 — 테스트에서 직접 호출) ────────────────────────────

    /**
     * PmsBlMartDocument에서 이 문서가 속해야 할 B/L-grain 비트맵 키 집합을 파생한다.
     *
     * 포함되는 키 종류:
     * - B/L-level 차원: jobDiv/bound (W1-A: 다른 차원 제거)
     * - has-flag(4종): freight/tax/slip/doc
     * - etd/eta 일버킷
     * - line(perfdt) 버킷: 속성(has-freight/has-tax/has-slip/fdc-TYPE) + W2 E3 composite
     * - W3 doc-exists: docs[]의 status별 dcx:status:* 비트맵
     */
    static Set<String> deriveMembershipKeys(PmsBlMartDocument doc, String prefix) {
        Set<String> keys = new HashSet<>();

        // B/L-level 차원 — jobDiv/bound만 잔존
        addDimKey(keys, prefix, PmsCountIndexKeys.DIM_JOBDIV, doc.getJobDiv());
        addDimKey(keys, prefix, PmsCountIndexKeys.DIM_BOUND,  doc.getBound());

        // has-flag
        if (doc.isHasFreightInput())    keys.add(PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_FREIGHT));
        if (doc.isHasTaxIssued())       keys.add(PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_TAX));
        if (doc.isHasSlipIssued())      keys.add(PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_SLIP));
        if (doc.isHasDocumentCreated()) keys.add(PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_DOC));

        // ETD/ETA 일버킷
        if (StringUtils.hasText(doc.getEtd())) keys.add(PmsCountIndexKeys.etdDayBitmap(prefix, doc.getEtd()));
        if (StringUtils.hasText(doc.getEta())) keys.add(PmsCountIndexKeys.etaDayBitmap(prefix, doc.getEta()));

        // line(perfdt) 버킷 — 속성 + W2 composite
        addLineKeys(keys, doc, prefix);

        // W3: docs[] 기반 B/L-grain doc-exists 키 파생
        addDocExistsKeys(keys, doc, prefix);

        return keys;
    }

    /** 단인자 오버로드 — 기존 테스트 호환용. */
    static Set<String> deriveMembershipKeys(PmsBlMartDocument doc) {
        return deriveMembershipKeys(doc, "pms:ix");
    }

    // ── RoaringBitmap 직렬화 헬퍼 (public — FreightPath/DocumentPath에서 참조) ──

    public static byte[] serialize(RoaringBitmap bitmap) {
        bitmap.runOptimize();
        ByteBuffer buf = ByteBuffer.allocate(bitmap.serializedSizeInBytes());
        bitmap.serialize(buf);
        return buf.array();
    }

    public static RoaringBitmap deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new RoaringBitmap();
        }
        RoaringBitmap bitmap = new RoaringBitmap();
        try {
            bitmap.deserialize(ByteBuffer.wrap(bytes));
        } catch (IOException e) {
            throw new IllegalStateException("RoaringBitmap deserialize 실패", e);
        }
        return bitmap;
    }

    // ── 내부 구현 ─────────────────────────────────────────────────────────────

    private void doApplyChanges(List<PmsBlMartDocument> oldDocs, List<PmsBlMartDocument> newDocs) {
        Map<String, DocMembership> oldMap = buildMembershipMap(oldDocs);
        Map<String, DocMembership> newMap = buildMembershipMap(newDocs);

        Set<String> allBlKeys = new HashSet<>();
        allBlKeys.addAll(oldMap.keySet());
        allBlKeys.addAll(newMap.keySet());

        Map<String, List<Integer>> toAdd    = new HashMap<>();
        Map<String, List<Integer>> toRemove = new HashMap<>();
        Map<String, List<Integer>> docToAdd    = new HashMap<>();
        Map<String, List<Integer>> docToRemove = new HashMap<>();
        Map<String, String> collapseToAdd    = new HashMap<>();
        List<String>        collapseToRemove = new ArrayList<>();

        for (String blKey : allBlKeys) {
            DocMembership oldM = oldMap.getOrDefault(blKey, DocMembership.EMPTY);
            DocMembership newM = newMap.getOrDefault(blKey, DocMembership.EMPTY);
            int ordinal = newM != DocMembership.EMPTY ? newM.ordinal : oldM.ordinal;

            diffSets(oldM.blBitmapKeys, newM.blBitmapKeys, ordinal, toAdd, toRemove);

            RoaringBitmap addedFdIds   = RoaringBitmap.andNot(newM.fdIds, oldM.fdIds);
            RoaringBitmap removedFdIds = RoaringBitmap.andNot(oldM.fdIds, newM.fdIds);

            PmsBlMartDocument newDoc = findDocByKey(newDocs, blKey);
            if (newDoc != null) {
                docSupport.addDocFdIdsToBitmapDiff(newDoc, addedFdIds, removedFdIds, ordinal,
                                                   docToAdd, docToRemove, collapseToAdd, collapseToRemove);
            } else if (!removedFdIds.isEmpty()) {
                PmsBlMartDocument oldDoc = findDocByKey(oldDocs, blKey);
                if (oldDoc != null) {
                    docSupport.addDocFdIdsToBitmapDiff(oldDoc, new RoaringBitmap(), removedFdIds, ordinal,
                                                       docToAdd, docToRemove, collapseToAdd, collapseToRemove);
                }
            }
        }

        redisWriter.applyBitmapDiff(toAdd, toRemove);
        redisWriter.applyBitmapDiff(docToAdd, docToRemove);
        redisWriter.applyCollapseHashDiff(collapseToAdd, collapseToRemove);

        // 성공 시 syncAt만 갱신 (complete 유지)
        redisTemplate.opsForHash().put(
            PmsCountIndexKeys.meta(prefix),
            PmsCountIndexKeys.META_SYNC_AT,
            String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, DocMembership> buildMembershipMap(List<PmsBlMartDocument> docs) {
        Map<String, DocMembership> map = new HashMap<>(docs.size() * 2);
        for (PmsBlMartDocument doc : docs) {
            if (PmsCountIndexKeys.isBlIdOverflow(doc.getBlId())) {
                handleBlIdOverflow(doc.getId(), doc.getBlId());
                continue;
            }
            int ordinal = PmsCountIndexKeys.toOrdinal(doc.getBlId(), doc.getBlType());
            Set<String> blKeys = deriveMembershipKeys(doc, prefix);
            RoaringBitmap fdIds = docSupport.deriveDocFdIds(doc);
            map.put(doc.getId(), new DocMembership(ordinal, blKeys, fdIds));
        }
        return map;
    }

    private void handleBlIdOverflow(String docId, Long blId) {
        try {
            redisTemplate.opsForValue().set(PmsCountIndexKeys.blOverflowFlag(prefix),
                                            "1".getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // intentionally ignored: overflow 플래그 set 실패는 warn으로 처리
        }
        if (!blOverflowWarned) {
            blOverflowWarned = true;
            log.warn("Count Index B/L ordinal overflow: docId={}, blId={} — 해당 B/L 생략. bl:overflow 플래그 설정.", docId, blId);
        }
    }

    private static PmsBlMartDocument findDocByKey(List<PmsBlMartDocument> docs, String blKey) {
        for (PmsBlMartDocument d : docs) {
            if (blKey.equals(d.getId())) return d;
        }
        return null;
    }

    private static void diffSets(
            Set<String> oldKeys, Set<String> newKeys,
            int ordinal,
            Map<String, List<Integer>> toAdd,
            Map<String, List<Integer>> toRemove) {

        for (String k : newKeys) {
            if (!oldKeys.contains(k)) toAdd.computeIfAbsent(k, ignored -> new ArrayList<>()).add(ordinal);
        }
        for (String k : oldKeys) {
            if (!newKeys.contains(k)) toRemove.computeIfAbsent(k, ignored -> new ArrayList<>()).add(ordinal);
        }
    }

    /**
     * freight line 버킷 키를 파생한다.
     *
     * 기존 속성 버킷:
     * - {p}:ln:pd:{day}:has-freight  (pd 비공백인 모든 라인)
     * - {p}:ln:pd:{day}:has-tax      (tax=true)
     * - {p}:ln:pd:{day}:has-slip     (slip=true)
     * - {p}:ln:pd:{day}:fdc-{TYPE}   (fdcType 비공백)
     * - {p}:ln:fdc:{TYPE}            (전역, pd 유무 무관)
     *
     * W2 E3 composite 버킷:
     * - {p}:ln:pd:{day}:c:{t}{s}:{TYPE}  (일 복합 — pd 보유 라인, 실적일자 범위 쿼리용)
     * - {p}:ln:c:{t}{s}:{TYPE}           (전역 복합 — 라인 전수, 무날짜 라인-술어 쿼리용)
     *
     * 전역 복합은 pd 유무와 무관하게 모든 라인에 1건씩 적재한다.
     * ETD 기간 등 perfDt 조건 없는 라인-술어 쿼리가 전역 복합을 조회하므로,
     * pd 보유 라인이 빠지면 해당 쿼리가 거의 0으로 과소집계된다.
     */
    private static void addLineKeys(Set<String> keys, PmsBlMartDocument doc, String prefix) {
        List<PmsBlLineEmbedded> lines = doc.getLines();
        if (lines == null || lines.isEmpty()) return;
        for (PmsBlLineEmbedded line : lines) {
            String pd = line.getPd();
            boolean hasPd = StringUtils.hasText(pd);
            String fdcType = line.getFdcType();

            if (hasPd) {
                // 기존 속성 버킷 (일별)
                keys.add(PmsCountIndexKeys.linePdAttrBitmap(prefix, pd, PmsCountIndexKeys.LINE_ATTR_HAS_FREIGHT));
                if (line.isTax())  keys.add(PmsCountIndexKeys.linePdAttrBitmap(prefix, pd, PmsCountIndexKeys.LINE_ATTR_HAS_TAX));
                if (line.isSlip()) keys.add(PmsCountIndexKeys.linePdAttrBitmap(prefix, pd, PmsCountIndexKeys.LINE_ATTR_HAS_SLIP));
                if (StringUtils.hasText(fdcType)) {
                    keys.add(PmsCountIndexKeys.linePdAttrBitmap(prefix, pd, PmsCountIndexKeys.LINE_FDC_PREFIX + fdcType));
                    keys.add(PmsCountIndexKeys.lineGlobalFdcBitmap(prefix, fdcType));
                }

                // 일 복합 버킷 (실적일자 범위 쿼리용)
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(
                    prefix, pd, line.isTax(), line.isSlip(), fdcType));
            } else {
                // pd 없는 라인: 전역 fdcType 버킷
                if (StringUtils.hasText(fdcType)) {
                    keys.add(PmsCountIndexKeys.lineGlobalFdcBitmap(prefix, fdcType));
                }
            }

            // 전역 복합 버킷 — pd 유무와 무관하게 모든 라인 전수 적재
            // (무날짜 라인-술어 쿼리, 예: ETD 기간 + documentTypes, 가 이 버킷을 조회한다)
            keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(
                prefix, line.isTax(), line.isSlip(), fdcType));
        }
    }

    private static void addDimKey(Set<String> keys, String prefix, String dim, String code) {
        if (StringUtils.hasText(code)) keys.add(PmsCountIndexKeys.dimBitmap(prefix, dim, code));
    }

    /**
     * W3: docs[]에서 B/L-grain doc-exists 키를 파생한다.
     *
     * 각 doc 원소의 status로 dcx:status:* 버킷 키를 생성한다.
     * - docs null/empty이면 아무 키도 추가하지 않는다.
     */
    private static void addDocExistsKeys(Set<String> keys, PmsBlMartDocument doc, String prefix) {
        List<PmsBlDocEmbedded> docs = doc.getDocs();
        if (docs == null || docs.isEmpty()) return;
        for (PmsBlDocEmbedded d : docs) {
            if (StringUtils.hasText(d.getStatus())) {
                keys.add(PmsCountIndexKeys.blDocStatusBitmap(prefix, d.getStatus()));
            }
        }
    }

    private record DocMembership(int ordinal, Set<String> blBitmapKeys, RoaringBitmap fdIds) {
        static final DocMembership EMPTY = new DocMembership(0, Set.of(), new RoaringBitmap());
    }
}
