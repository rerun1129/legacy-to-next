package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Count Index 벌크 빌드 전담 클래스.
 *
 * <p>전략: JVM 메모리에 누적 후 일괄 SET.
 * <ol>
 *   <li>meta 삭제(not-ready 상태 진입)
 *   <li>prefix 네임스페이스 SCAN+UNLINK 초기화
 *   <li>pms_bl_mart를 단일 스레드로 페이징 스캔하며 JVM 누적
 *       — HashMap&lt;bitmapKey, RoaringBitmap&gt; + dc:bl용 growable int[] 배열
 *   <li>누적 완료 후 일괄 SET(비트맵) + HSET(dc:bl collapse hash)
 *   <li>markComplete()
 * </ol>
 *
 * <p>dc:bl collapse hash 구조:
 * fdId-인덱스 배열: index=ordinal, value=fdId (없으면 -1).
 * 6M 엔트리 기준 박싱 없는 int[] 사용(HashMap&lt;Integer,Integer&gt; 대비 메모리 1/4).
 */
@Slf4j
@Component
// master(pms.mart.enabled)와 하위 플래그 모두 true일 때만 활성 — mart off 시 계열 전체 off
@ConditionalOnProperty(prefix = "pms.mart", name = {"enabled", "count-index.enabled"}, havingValue = "true")
public class PmsCountIndexBulkBuilder {

    /** dc:bl HSET 청크 크기 (field 개수 기준). */
    private static final int DC_BL_HSET_CHUNK = 10_000;
    /** 비트맵 SET pipeline 청크 크기. */
    private static final int BITMAP_SET_CHUNK = 2_000;
    /** fdId→ordinal 배열 초기 크기. 2배 확장 전략. */
    private static final int FD_IDX_INITIAL_SIZE = 1 << 20; // 1M
    /** fdId→ordinal 배열에서 "비어 있음" 표시값. */
    private static final int FD_IDX_EMPTY = -1;
    /** 진행 로그 출력 단위. */
    private static final long LOG_INTERVAL = 100_000L;

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final PmsCountIndexMaintainer maintainer;
    private final PmsCountIndexDocSupport docSupport;
    private final PmsMartProperties props;
    private final String prefix;

    PmsCountIndexBulkBuilder(
            MongoTemplate mongoTemplate,
            RedisTemplate<String, byte[]> redisTemplate,
            PmsCountIndexMaintainer maintainer,
            PmsMartProperties props) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.maintainer    = maintainer;
        this.props         = props;
        this.prefix        = props.getCountIndex().getKeyPrefix();
        // docSupport는 fdId overflow 처리를 위해 redisTemplate 필요
        this.docSupport    = new PmsCountIndexDocSupport(redisTemplate, this.prefix);
    }

    // ── 공개 API ──────────────────────────────────────────────────────────────

    /**
     * Mart 전체 문서를 JVM에 누적 후 Redis에 일괄 기록한다.
     *
     * 실패 시 meta를 기록하지 않아 not-ready 상태를 유지한다(Mongo 폴백 유지).
     */
    public void rebuildFromMart() {
        log.info("Count Index bulk rebuild 시작: prefix={}", prefix);

        // (1) meta 삭제 → 빌드 내내 not-ready
        redisTemplate.delete(PmsCountIndexKeys.meta(prefix));

        // (2) 네임스페이스 초기화
        maintainer.flushAll();

        // (3) JVM 누적
        Map<String, RoaringBitmap> bitmapAccum = new HashMap<>();
        // fdId → ordinal 매핑: index=fdId, value=ordinal (FD_IDX_EMPTY=미등록)
        // Java에서 배열 재할당을 위해 단원소 배열에 담는다
        int[] fdIdToOrdinal = new int[FD_IDX_INITIAL_SIZE];
        Arrays.fill(fdIdToOrdinal, FD_IDX_EMPTY);
        int[][] fdIdToOrdinalHolder = { fdIdToOrdinal };

        int batchSize = props.getRebuild().getBatchSize();
        long total    = 0;
        // skip 기반 O(n²) 대신 _id keyset 커서로 O(n) range scan.
        // pms_bl_mart._id는 String("HOUSE#123"/"MASTER#456")이며 기본 인덱스 사용.
        String lastId = null;

        while (true) {
            Query q = new Query();
            if (lastId != null) {
                q.addCriteria(Criteria.where("_id").gt(lastId));
            }
            q.with(Sort.by(Sort.Direction.ASC, "_id")).limit(batchSize);
            List<PmsBlMartDocument> batch = mongoTemplate.find(q, PmsBlMartDocument.class);
            if (batch.isEmpty()) break;

            for (PmsBlMartDocument doc : batch) {
                if (PmsCountIndexKeys.isBlIdOverflow(doc.getBlId())) {
                    // overflow 플래그 설정 후 생략 — 빌드 후 isReady()에서 차단
                    redisTemplate.opsForValue().set(PmsCountIndexKeys.blOverflowFlag(prefix),
                                                    "1".getBytes(StandardCharsets.UTF_8));
                    log.warn("Count Index bulk: B/L ordinal overflow docId={}, blId={} — 생략", doc.getId(), doc.getBlId());
                    continue;
                }
                int ordinal = PmsCountIndexKeys.toOrdinal(doc.getBlId(), doc.getBlType());

                // B/L-grain 비트맵 누적
                Set<String> blKeys = PmsCountIndexMaintainer.deriveMembershipKeys(doc, prefix);
                for (String k : blKeys) {
                    bitmapAccum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(ordinal);
                }

                // doc fdId-grain 비트맵 누적
                accumulateDocBitmaps(doc, ordinal, bitmapAccum, fdIdToOrdinalHolder);
            }

            total += batch.size();
            lastId = batch.get(batch.size() - 1).getId();
            if (total % LOG_INTERVAL == 0) {
                log.info("Count Index bulk rebuild 진행: {}건 누적", total);
            }
        }

        log.info("Count Index bulk rebuild 누적 완료: {}건. Redis 일괄 기록 시작.", total);

        // (4) 일괄 기록
        writeBitmaps(bitmapAccum);
        writeCollapseHash(fdIdToOrdinalHolder[0]);

        // (5) markComplete
        maintainer.markComplete();
        log.info("Count Index bulk rebuild 완료: {}건, 비트맵키={}개", total, bitmapAccum.size());
    }

    // ── 누적 헬퍼 ─────────────────────────────────────────────────────────────

    private void accumulateDocBitmaps(
            PmsBlMartDocument doc,
            int ordinal,
            Map<String, RoaringBitmap> bitmapAccum,
            int[][] fdIdToOrdinalHolder) {

        List<PmsBlDocEmbedded> docList = doc.getDocs();
        if (docList == null || docList.isEmpty()) return;

        for (PmsBlDocEmbedded d : docList) {
            Long fdId = d.getFdId();
            if (fdId == null || fdId < 0 || fdId > Integer.MAX_VALUE) continue;
            int fdIdInt = fdId.intValue();

            // fdId-grain 비트맵 키 누적
            for (String k : docSupport.deriveDocBitmapKeysForDoc(d)) {
                bitmapAccum.computeIfAbsent(k, ignored -> new RoaringBitmap()).add(fdIdInt);
            }

            // fdId → ordinal 배열 갱신 (필요시 2배 확장)
            fdIdToOrdinalHolder[0] = ensureCapacity(fdIdToOrdinalHolder[0], fdIdInt + 1);
            fdIdToOrdinalHolder[0][fdIdInt] = ordinal;
        }
    }

    /**
     * fdId → ordinal 배열 용량 확보. 필요시 현재 길이의 2배로 확장.
     */
    private static int[] ensureCapacity(int[] arr, int minLen) {
        if (arr.length >= minLen) return arr;
        int newLen = Math.max(arr.length * 2, minLen);
        int[] next = new int[newLen];
        Arrays.fill(next, FD_IDX_EMPTY);
        System.arraycopy(arr, 0, next, 0, arr.length);
        return next;
    }

    // ── Redis 기록 ────────────────────────────────────────────────────────────

    /**
     * 비트맵 누적 맵을 pipeline SET으로 기록한다. ≤BITMAP_SET_CHUNK 키 단위 청크.
     */
    private void writeBitmaps(Map<String, RoaringBitmap> bitmapAccum) {
        List<Map.Entry<String, RoaringBitmap>> entries = new ArrayList<>(bitmapAccum.entrySet());
        for (int start = 0; start < entries.size(); start += BITMAP_SET_CHUNK) {
            int end = Math.min(start + BITMAP_SET_CHUNK, entries.size());
            List<Map.Entry<String, RoaringBitmap>> chunk = entries.subList(start, end);
            redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
                for (Map.Entry<String, RoaringBitmap> e : chunk) {
                    byte[] val = PmsCountIndexMaintainer.serialize(e.getValue());
                    conn.stringCommands().set(e.getKey().getBytes(StandardCharsets.UTF_8), val);
                }
                return null;
            });
        }
        log.info("Count Index bulk: 비트맵 {}개 기록 완료", bitmapAccum.size());
    }

    /**
     * fdId → ordinal 배열에서 dc:bl HSET을 DC_BL_HSET_CHUNK 단위 pipeline으로 기록한다.
     * value=FD_IDX_EMPTY인 항목은 건너뛴다.
     */
    private void writeCollapseHash(int[] fdIdToOrdinal) {
        String hashKey = PmsCountIndexKeys.docCollapseHash(prefix);
        Map<byte[], byte[]> chunk = new HashMap<>(DC_BL_HSET_CHUNK * 2);
        long written = 0;

        for (int fdId = 0; fdId < fdIdToOrdinal.length; fdId++) {
            int ord = fdIdToOrdinal[fdId];
            if (ord == FD_IDX_EMPTY) continue;
            chunk.put(
                String.valueOf(fdId).getBytes(StandardCharsets.UTF_8),
                String.valueOf(ord).getBytes(StandardCharsets.UTF_8));
            if (chunk.size() >= DC_BL_HSET_CHUNK) {
                flushCollapseChunk(hashKey, chunk);
                written += chunk.size();
                chunk = new HashMap<>(DC_BL_HSET_CHUNK * 2);
            }
        }
        if (!chunk.isEmpty()) {
            flushCollapseChunk(hashKey, chunk);
            written += chunk.size();
        }
        log.info("Count Index bulk: dc:bl collapse hash {}개 엔트리 기록 완료", written);
    }

    private void flushCollapseChunk(String hashKey, Map<byte[], byte[]> chunk) {
        final Map<byte[], byte[]> toFlush = chunk;
        redisTemplate.executePipelined((RedisCallback<Object>) conn -> {
            conn.hashCommands().hMSet(hashKey.getBytes(StandardCharsets.UTF_8), toFlush);
            return null;
        });
    }
}
