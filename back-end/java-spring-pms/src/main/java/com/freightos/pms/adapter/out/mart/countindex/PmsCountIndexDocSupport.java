package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.adapter.out.mart.document.PmsBlDocEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Phase C doc(fdId-grain) 비트맵 적재 보조 로직 (package-private).
 *
 * PmsCountIndexMaintainer의 500줄 초과를 방지하기 위해 분리된 헬퍼.
 * fdId-grain 비트맵 키 파생, overflow 감지, collapse hash diff 계산을 담당한다.
 *
 * 모든 메서드는 인스턴스(prefix·redisTemplate 의존)이거나 static(순수 파생).
 */
@Slf4j
final class PmsCountIndexDocSupport {

    /** overflow 경고 1회 제한 플래그. */
    private volatile boolean overflowWarned = false;

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final String prefix;

    PmsCountIndexDocSupport(RedisTemplate<String, byte[]> redisTemplate, String prefix) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
    }

    // ── fdId 집합 파생 ─────────────────────────────────────────────────────────

    /**
     * B/L 문서의 docs[] 배열에서 유효한 fdId들을 RoaringBitmap으로 반환한다.
     * fdId null/음수/MAX_INT 초과는 overflow 처리 후 생략.
     */
    RoaringBitmap deriveDocFdIds(PmsBlMartDocument doc) {
        List<PmsBlDocEmbedded> docList = doc.getDocs();
        RoaringBitmap result = new RoaringBitmap();
        if (docList == null || docList.isEmpty()) {
            return result;
        }
        for (PmsBlDocEmbedded d : docList) {
            Long fdId = d.getFdId();
            if (fdId == null || fdId < 0 || fdId > Integer.MAX_VALUE) {
                handleFdIdOverflow(doc.getId(), fdId);
                continue;
            }
            result.add(fdId.intValue());
        }
        return result;
    }

    /** overflow 발생 시 플래그 설정 + warn(1회). */
    private void handleFdIdOverflow(String blKey, Long fdId) {
        redisTemplate.opsForValue().set(PmsCountIndexKeys.docOverflowFlag(prefix),
                                        "1".getBytes(StandardCharsets.UTF_8));
        if (!overflowWarned) {
            overflowWarned = true;
            log.warn("Count Index doc fdId overflow: blKey={}, fdId={} — 해당 doc 생략. dc:overflow 플래그 설정.",
                     blKey, fdId);
        }
    }

    // ── doc diff 계산 ─────────────────────────────────────────────────────────

    /**
     * 추가/제거된 fdId들로부터 doc 비트맵 diff와 collapse hash diff를 계산한다.
     *
     * 추가된 fdId: 해당 doc 원소의 멤버십 키 → docToAdd 누적 + collapseToAdd 등록.
     * 제거된 fdId: 재현 가능한 범위(현 docs[] 등장 키) 전체에서 remove → docToRemove 누적
     *              + collapseToRemove 등록.
     */
    void addDocFdIdsToBitmapDiff(
            PmsBlMartDocument martDoc,
            RoaringBitmap addedFdIds,
            RoaringBitmap removedFdIds,
            int blOrdinal,
            Map<String, List<Integer>> docToAdd,
            Map<String, List<Integer>> docToRemove,
            Map<String, String> collapseToAdd,
            List<String> collapseToRemove) {

        List<PmsBlDocEmbedded> docList = martDoc.getDocs();
        if (docList == null || docList.isEmpty()) {
            return;
        }

        // 추가된 fdId: 해당 doc의 멤버십 키 산출
        for (PmsBlDocEmbedded d : docList) {
            Long fdId = d.getFdId();
            if (fdId == null || fdId < 0 || fdId > Integer.MAX_VALUE) continue;
            int fdIdInt = fdId.intValue();
            if (addedFdIds.contains(fdIdInt)) {
                for (String k : deriveDocBitmapKeysForDoc(d)) {
                    docToAdd.computeIfAbsent(k, ignored -> new ArrayList<>()).add(fdIdInt);
                }
                collapseToAdd.put(String.valueOf(fdIdInt), String.valueOf(blOrdinal));
            }
        }

        // 제거된 fdId: 현재 문서에 등장하는 모든 dc:* 키 패턴에서 remove 시도
        // (과도 remove는 비트맵에 없으면 무해)
        if (!removedFdIds.isEmpty()) {
            Set<String> allDcPatternKeys = collectAllDcPatternKeys(martDoc);
            int[] removedArr = removedFdIds.toArray();
            for (String k : allDcPatternKeys) {
                for (int fdIdInt : removedArr) {
                    docToRemove.computeIfAbsent(k, ignored -> new ArrayList<>()).add(fdIdInt);
                }
            }
            for (int fdIdInt : removedArr) {
                collapseToRemove.add(String.valueOf(fdIdInt));
            }
        }
    }

    // ── doc 비트맵 키 파생 ────────────────────────────────────────────────────

    /**
     * docs[] 원소 하나에서 fdId-grain 비트맵 키 목록을 파생한다.
     * 값이 공백이면 해당 키 생략.
     */
    List<String> deriveDocBitmapKeysForDoc(PmsBlDocEmbedded d) {
        List<String> keys = new ArrayList<>();
        keys.add(PmsCountIndexKeys.docAllBitmap(prefix));
        if (StringUtils.hasText(d.getDocType())) {
            keys.add(PmsCountIndexKeys.docTypeBitmap(prefix, d.getDocType()));
        }
        if (StringUtils.hasText(d.getStatus())) {
            keys.add(PmsCountIndexKeys.docStatusBitmap(prefix, d.getStatus()));
        }
        if (d.isGrouped()) {
            keys.add(PmsCountIndexKeys.docGroupedBitmap(prefix));
        }
        if (StringUtils.hasText(d.getDocDt())) {
            keys.add(PmsCountIndexKeys.docDtDayBitmap(prefix, d.getDocDt()));
        }
        if (StringUtils.hasText(d.getPerfPd())) {
            keys.add(PmsCountIndexKeys.docPerfPdDayBitmap(prefix, d.getPerfPd()));
        }
        return keys;
    }

    /**
     * 제거된 fdId가 속할 수 있는 모든 dc:* 키 패턴을 반환한다.
     * 현재 문서의 docs[]에서 등장하는 실제 값들로 한정.
     */
    private Set<String> collectAllDcPatternKeys(PmsBlMartDocument martDoc) {
        Set<String> keys = new HashSet<>();
        keys.add(PmsCountIndexKeys.docAllBitmap(prefix));
        keys.add(PmsCountIndexKeys.docGroupedBitmap(prefix));

        List<PmsBlDocEmbedded> docList = martDoc.getDocs();
        if (docList == null) return keys;
        for (PmsBlDocEmbedded d : docList) {
            if (StringUtils.hasText(d.getDocType()))
                keys.add(PmsCountIndexKeys.docTypeBitmap(prefix, d.getDocType()));
            if (StringUtils.hasText(d.getStatus()))
                keys.add(PmsCountIndexKeys.docStatusBitmap(prefix, d.getStatus()));
            if (StringUtils.hasText(d.getDocDt()))
                keys.add(PmsCountIndexKeys.docDtDayBitmap(prefix, d.getDocDt()));
            if (StringUtils.hasText(d.getPerfPd()))
                keys.add(PmsCountIndexKeys.docPerfPdDayBitmap(prefix, d.getPerfPd()));
        }
        return keys;
    }
}
