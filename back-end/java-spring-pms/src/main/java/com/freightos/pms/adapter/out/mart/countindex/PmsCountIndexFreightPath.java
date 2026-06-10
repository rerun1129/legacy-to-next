package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Phase B freight 경로 판정·계산 협력자 (package-private).
 *
 * PmsRedisExactCountProvider에서 위임받아 perfdt 일·속성 버킷 기반
 * freight/tax/slip basis의 정확 count를 계산한다.
 *
 * 지원 조건 판정은 {@link #computeFreightCount} 내부에서 수행한다:
 * 지원 불가이면 null을 반환(Mongo 폴백 신호).
 *
 * 계산 전략:
 * - perfDt 범위(양쪽) 있고 라인 술어(issued/types/basis) 있으면:
 *     E3 composite 일버킷 OR → B/L 차원 AND → [W3 doc-exists AND] → cardinality
 * - perfDt 범위(양쪽) 있고 라인 술어 없으면:
 *     일·속성 버킷 OR → B/L 차원 AND → [W3 doc-exists AND] → cardinality
 * - perfDt 없고 라인 술어 있으면:
 *     E3 전역 composite 버킷 OR → ETD/ETA 및 차원 AND → [W3 doc-exists AND] → cardinality
 * - perfDt 없고 FREIGHT_INPUT && documentTypes 있고 issued 없으면:
 *     전역 fdc 버킷 OR → dim AND → [W3 doc-exists AND] (기존 fast-path 유지)
 * - W3 P6: perfDt 없고 라인 술어도 없고 doc 술어(status/grouped)만 있으면:
 *     has-flag 비트맵 → ETD/ETA AND → dim AND → W3 doc-exists AND → cardinality
 * - 그 외: null (Mongo 폴백)
 *
 * W1-A: hasBlNoFilter/taxType/documentNoLike/groupFinancialNo/operator/documentDtFrom/To/financialDocType 규칙 제거.
 * W2: issued 필터를 E3 composite 버킷({p}:ln:pd:{day}:c:{t}{s}{i}:{TYPE})으로 처리.
 *     issued 있으면 compound 버킷(tax/slip/issued 조합 × fdcType 변형)을 OR하여 정확 count 반환.
 * W3: documentStatus/grouped 있으면 fdId-grain collapse(cap 초과 폴백) 대신 B/L-grain dcx:* 비트맵 AND.
 *     dc:overflow 게이팅 보존: docOverflowFlag 존재 시 null(Mongo 폴백).
 *
 * 헬퍼 위임: 키 수집·비트맵 연산·날짜 유틸은 {@link PmsCountIndexFreightPathSupport}에 분리.
 * (500줄 강제 분리 규칙 적용)
 */
@Slf4j
final class PmsCountIndexFreightPath {

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final PmsMartProperties props;
    private final PmsCountIndexFreightPathSupport support;

    PmsCountIndexFreightPath(RedisTemplate<String, byte[]> redisTemplate, PmsMartProperties props) {
        this.redisTemplate = redisTemplate;
        this.props         = props;
        this.support       = new PmsCountIndexFreightPathSupport(redisTemplate, props);
    }

    // ── 공개(package-private) API ─────────────────────────────────────────────

    /**
     * freight basis 경로 계산을 시도한다.
     *
     * null 반환(Mongo 폴백) 조건:
     * - basis ∉ {FREIGHT_INPUT, TAX_ISSUED, SLIP_ISSUED}
     * - line-accel OFF
     * - dateKind=="PERFORMANCE" && (dateFrom||dateTo): line-level → Mongo 처리
     * - documentDtFrom/To: doc 레벨 필터 → freight 버킷 미포함
     * - perfDt 범위 한쪽만 존재 (open range)
     * - 일수 > maxDayBuckets
     * - 변형 키 수 > maxDayBuckets×8 (전역 composite 키 수 가드)
     * - W3: documentStatus/grouped 있을 때 dc:overflow 플래그 존재 → Mongo 폴백
     * - perfDt 없고 라인 술어도 없고 doc 술어도 없고 FREIGHT_INPUT 타입필터도 없는 형태
     *
     * W2: issued 필터는 E3 composite 버킷으로 처리(null 반환하지 않음).
     * W3: documentStatus/grouped는 B/L-grain dcx:* 비트맵 AND로 처리(null 반환하지 않음).
     *     dc:overflow 게이팅 보존.
     * E3: TAX/SLIP+types 조합의 needsLineGrainCorrelation null 규칙 제거 — 복합버킷 경로로 처리.
     *     perfDt 없는 경우에도 전역 composite 버킷(lineCompositeGlobalBitmap)으로 처리.
     */
    Long computeFreightCount(SearchPmsPerformanceCommand cmd, String prefix) {
        AggregationBasis basis = cmd.effectiveBasis();

        // basis 대상 여부
        if (basis != AggregationBasis.FREIGHT_INPUT
                && basis != AggregationBasis.TAX_ISSUED
                && basis != AggregationBasis.SLIP_ISSUED) {
            return null;
        }

        // line-accel OFF이면 line 버킷 미적재 → 폴백
        if (!props.getLineAccel().isEnabled()) {
            return null;
        }

        // documentDtFrom/To: doc 레벨 필터 — freight 버킷 미포함
        if (StringUtils.hasText(cmd.documentDtFrom()) || StringUtils.hasText(cmd.documentDtTo())) return null;

        // dateKind=="PERFORMANCE" + dateFrom/To: line-level 실적일자 → Mongo 처리
        if ("PERFORMANCE".equals(cmd.dateKind())
                && (StringUtils.hasText(cmd.dateFrom()) || StringUtils.hasText(cmd.dateTo()))) {
            return null;
        }

        boolean hasDocTypes      = cmd.documentTypes() != null && !cmd.documentTypes().isEmpty();
        boolean hasIssued        = StringUtils.hasText(cmd.issued());
        // 라인 술어: issued·documentTypes·TAX·SLIP basis — 복합버킷이 필요한 조건
        boolean hasLinePredicate = hasIssued || hasDocTypes
                || basis == AggregationBasis.TAX_ISSUED || basis == AggregationBasis.SLIP_ISSUED;

        // perfDt 범위 처리
        String perfFrom     = cmd.performanceDtFrom();
        String perfTo       = cmd.performanceDtTo();
        boolean hasPerfFrom = StringUtils.hasText(perfFrom);
        boolean hasPerfTo   = StringUtils.hasText(perfTo);

        if (hasPerfFrom || hasPerfTo) {
            // open range (한쪽만) → null
            if (hasPerfFrom != hasPerfTo) return null;

            // 일수 초과 검사
            long dayCount = support.countDays(perfFrom, perfTo);
            if (dayCount < 0) return null; // 파싱 실패
            if (dayCount > props.getCountIndex().getMaxDayBuckets()) {
                log.debug("Count Index freight: perfDt 일수({}) maxDayBuckets 초과 → Mongo 폴백", dayCount);
                return null;
            }

            // 라인 술어(issued/types/TAX·SLIP basis) 있으면 E3 composite 일버킷 경로
            if (hasLinePredicate) {
                return computeWithCompositeAndDoc(cmd, prefix, basis, perfFrom, perfTo, hasDocTypes);
            }
            return computeWithPerfDtRange(cmd, prefix, basis, perfFrom, perfTo, hasDocTypes);
        }

        // perfDt 없는 경우 ─────────────────────────────────────────────────────

        // 라인 술어 있으면 전역 composite 버킷 경로
        if (hasLinePredicate) {
            return computeWithGlobalCompositeAndDoc(cmd, prefix, basis, hasDocTypes);
        }

        // FREIGHT_INPUT && documentTypes 있으면 전역 fdc 버킷 fast-path (라인 술어 없음)
        if (basis == AggregationBasis.FREIGHT_INPUT && hasDocTypes) {
            return computeWithGlobalFdc(cmd, prefix, hasDocTypes);
        }

        // W3: 라인 술어·타입 필터 없이 doc-exists 술어(status/grouped)만 있는 형태 — B/L-grain 즉답
        if (StringUtils.hasText(cmd.documentStatus()) || StringUtils.hasText(cmd.grouped())) {
            return computeWithBlDocOnly(cmd, prefix, basis);
        }

        // perfDt 없고 라인 술어도 없고 doc 술어도 없고 타입 필터도 없음 → Mongo 폴백
        return null;
    }

    // ── 내부 계산 ─────────────────────────────────────────────────────────────

    /**
     * perfDt 범위(양쪽 확정) + attr 기반 lineSet 구성 → dim AND → [E2 doc AND] → cardinality.
     */
    private Long computeWithPerfDtRange(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            String perfFrom,
            String perfTo,
            boolean hasDocTypes) {

        List<String> attrs = resolveAttrs(basis, cmd, hasDocTypes);
        if (attrs == null) return null;

        List<String> lineKeys = support.buildPerfDtLineKeys(prefix, perfFrom, perfTo, attrs);
        if (lineKeys == null) return null;

        List<String> etdEtaKeys = support.collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        List<String> dimKeys = support.collectDimKeys(cmd, prefix);

        List<String> allKeys = support.buildAllKeysList(lineKeys, etdEtaKeys, dimKeys);
        if (allKeys.isEmpty()) return null;

        Map<String, byte[]> keyToBytes = support.multiGetAsMap(allKeys);
        if (keyToBytes == null) return null;

        RoaringBitmap lineSet = support.orBitmaps(lineKeys, keyToBytes);
        RoaringBitmap dateSet = support.orBitmaps(etdEtaKeys, keyToBytes);

        RoaringBitmap result = lineSet;
        if (result == null) return 0L;
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);
        result = support.andDims(result, dimKeys, keyToBytes);

        return andDocComponent(result, cmd, prefix);
    }

    /**
     * 라인 술어 있고 perfDt 있는 경로(E3 composite 일버킷) → [E2 doc AND] → cardinality.
     */
    private Long computeWithCompositeAndDoc(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            String perfFrom,
            String perfTo,
            boolean hasDocTypes) {

        RoaringBitmap bitmap = support.computeWithCompositeBitmap(cmd, prefix, basis, perfFrom, perfTo, hasDocTypes);
        if (bitmap == null) return null;
        return andDocComponent(bitmap, cmd, prefix);
    }

    /**
     * perfDt 없고 라인 술어 있는 경로(E3 전역 composite 버킷) → [E2 doc AND] → cardinality.
     *
     * issued/types/TAX·SLIP basis 조합에 맞는 전역 composite 키를 열거해 OR → ETD/ETA AND
     * → 차원 AND → [E2 doc AND] → cardinality.
     * 변형 키 수 가드 초과·issued 미존재 등 지원 불가 시 null.
     */
    private Long computeWithGlobalCompositeAndDoc(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            boolean hasDocTypes) {

        RoaringBitmap bitmap = support.computeWithCompositeGlobalBitmap(cmd, prefix, basis, hasDocTypes);
        if (bitmap == null) return null;
        return andDocComponent(bitmap, cmd, prefix);
    }

    /**
     * perfDt 없고 FREIGHT_INPUT && 서류타입 필터가 있고 issued 없는 fast-path.
     * E2: documentStatus/grouped 있으면 doc-grain blOrdinal AND 적용.
     */
    private Long computeWithGlobalFdc(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            boolean hasDocTypes) {

        List<String> fdcKeys = new ArrayList<>();
        if (hasDocTypes) {
            for (String t : cmd.documentTypes()) {
                if (StringUtils.hasText(t)) {
                    fdcKeys.add(PmsCountIndexKeys.lineGlobalFdcBitmap(prefix, t));
                }
            }
        }

        if (fdcKeys.isEmpty()) return null;

        List<String> etdEtaKeys = support.collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        List<String> dimKeys = support.collectDimKeys(cmd, prefix);
        List<String> allKeys = support.buildAllKeysList(fdcKeys, etdEtaKeys, dimKeys);

        Map<String, byte[]> keyToBytes = support.multiGetAsMap(allKeys);
        if (keyToBytes == null) return null;

        RoaringBitmap fdcSet  = support.orBitmaps(fdcKeys, keyToBytes);
        RoaringBitmap dateSet = support.orBitmaps(etdEtaKeys, keyToBytes);

        RoaringBitmap result = fdcSet;
        if (result == null) return 0L;
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);
        result = support.andDims(result, dimKeys, keyToBytes);

        return andDocComponent(result, cmd, prefix);
    }

    // ── W3 doc-exists 컴포넌트 AND ──────────────────────────────────────────

    /**
     * W3: freight lineSet 결과에 B/L-grain doc-exists 비트맵 AND를 적용한다.
     *
     * documentStatus/grouped 없으면 lineSet cardinality를 그대로 반환한다.
     * dc:overflow 플래그 존재 시 null(Mongo 폴백) — 같은 빌드 파이프라인 산물이므로 게이팅 보존.
     *
     * grouped 미인식값(Y/N 외): 무시(기존 의미 유지).
     * - hasStatus && groupedYn → sg 키(status ∧ grouped) 1개 AND
     * - hasStatus만             → status 키 AND
     * - groupedYn만             → grouped 키 AND
     * - 둘 다 없음(미인식 grouped 단독 포함) → lineSet cardinality 그대로
     */
    private Long andDocComponent(RoaringBitmap lineSet, SearchPmsPerformanceCommand cmd, String prefix) {
        boolean hasDocPredicate = StringUtils.hasText(cmd.documentStatus()) || StringUtils.hasText(cmd.grouped());
        if (!hasDocPredicate) {
            return (long) lineSet.getCardinality();
        }

        // W3: dc:overflow 게이팅 — doc-exists 키도 같은 빌드 파이프라인 산물
        byte[] overflowFlag = redisTemplate.opsForValue().get(PmsCountIndexKeys.docOverflowFlag(prefix));
        if (overflowFlag != null) {
            log.debug("Count Index freight W3: dc:overflow 플래그 존재 → Mongo 폴백");
            return null;
        }

        boolean hasStatus = StringUtils.hasText(cmd.documentStatus());
        boolean groupedY  = "Y".equals(cmd.grouped());
        boolean groupedN  = "N".equals(cmd.grouped());
        boolean groupedYn = groupedY || groupedN;

        String docExistsKey;
        if (hasStatus && groupedYn) {
            // W3: status ∧ grouped same-doc 복합 키
            docExistsKey = PmsCountIndexKeys.blDocStatusGroupedBitmap(prefix, cmd.documentStatus(), groupedY);
        } else if (hasStatus) {
            docExistsKey = PmsCountIndexKeys.blDocStatusBitmap(prefix, cmd.documentStatus());
        } else if (groupedYn) {
            docExistsKey = PmsCountIndexKeys.blDocGroupedBitmap(prefix, groupedY);
        } else {
            // 미인식 grouped 단독 — 술어 무시, lineSet 그대로
            return (long) lineSet.getCardinality();
        }

        byte[] docExistsBytes = redisTemplate.opsForValue().get(docExistsKey);
        RoaringBitmap docExists = PmsCountIndexMaintainer.deserialize(docExistsBytes);

        return (long) RoaringBitmap.and(lineSet, docExists).getCardinality();
    }

    /**
     * W3 P6: 라인 술어·타입 필터 없이 doc-exists 술어(status/grouped)만 있는 경우의 즉답 경로.
     *
     * has-flag 비트맵 → ETD/ETA AND → dim AND → andDocComponent 의 B/L-grain doc-exists AND.
     * flag 비트맵이 시작점이므로 무필터 전체(flag만+doc술어)도 안전하게 동작한다.
     *
     * null 반환 조건:
     * - basis에 해당하는 has-flag 키 fetch 후 collectEtdEtaKeys가 null(maxDistinctScan 초과)
     */
    private Long computeWithBlDocOnly(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis) {

        // 1. basis별 has-flag 비트맵
        String flagKey = switch (basis) {
            case FREIGHT_INPUT -> PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_FREIGHT);
            case TAX_ISSUED    -> PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_TAX);
            case SLIP_ISSUED   -> PmsCountIndexKeys.hasFlagBitmap(prefix, PmsCountIndexKeys.FLAG_SLIP);
            default            -> null;
        };
        if (flagKey == null) return null;

        // 2. ETD/ETA 키 — null이면 maxDistinctScan 초과(Mongo 폴백)
        List<String> etdEtaKeys = support.collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        // 3. dim 키
        List<String> dimKeys = support.collectDimKeys(cmd, prefix);

        // 4. 모든 키 MGET
        List<String> allKeys = support.buildAllKeysList(List.of(flagKey), etdEtaKeys, dimKeys);
        Map<String, byte[]> keyToBytes = support.multiGetAsMap(allKeys);
        if (keyToBytes == null) return null;

        // 5. flag 비트맵 시작, dateSet OR → AND, dims AND
        byte[] flagBytes = keyToBytes.get(flagKey);
        RoaringBitmap result = PmsCountIndexMaintainer.deserialize(flagBytes);

        RoaringBitmap dateSet = support.orBitmaps(etdEtaKeys, keyToBytes);
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);
        result = support.andDims(result, dimKeys, keyToBytes);

        // 6. W3 doc-exists AND
        return andDocComponent(result, cmd, prefix);
    }

    // ── 속성 결정 ─────────────────────────────────────────────────────────────

    private List<String> resolveAttrs(AggregationBasis basis, SearchPmsPerformanceCommand cmd, boolean hasDocTypes) {
        return switch (basis) {
            case TAX_ISSUED   -> List.of(PmsCountIndexKeys.LINE_ATTR_HAS_TAX);
            case SLIP_ISSUED  -> List.of(PmsCountIndexKeys.LINE_ATTR_HAS_SLIP);
            case FREIGHT_INPUT -> resolveFreightAttrs(cmd, hasDocTypes);
            default -> null;
        };
    }

    private List<String> resolveFreightAttrs(SearchPmsPerformanceCommand cmd, boolean hasDocTypes) {
        if (!hasDocTypes) {
            return List.of(PmsCountIndexKeys.LINE_ATTR_HAS_FREIGHT);
        }
        List<String> attrs = new ArrayList<>();
        for (String t : cmd.documentTypes()) {
            if (StringUtils.hasText(t)) attrs.add(PmsCountIndexKeys.LINE_FDC_PREFIX + t);
        }
        return attrs.isEmpty() ? null : attrs;
    }

}
