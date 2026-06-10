package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
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
 * - perfDt 범위(양쪽) 있으면: 일·속성 버킷 OR → B/L 차원 AND 묶음 → cardinality
 * - perfDt 없고 FREIGHT_INPUT && (documentTypes||financialDocType) 있으면: 전역 fdc 버킷 OR → dim AND
 * - 그 외: null (Mongo 폴백)
 *
 * 일자 열거는 LocalDate.parse(yyyyMMdd)로 처리한다.
 * Date.now() 등 시계 의존 없이 결정적 로직만 사용한다.
 */
@Slf4j
final class PmsCountIndexFreightPath {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final PmsMartProperties props;

    PmsCountIndexFreightPath(RedisTemplate<String, byte[]> redisTemplate, PmsMartProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    // ── 공개(package-private) API ─────────────────────────────────────────────

    /**
     * freight basis 경로 계산을 시도한다.
     *
     * null 반환(Mongo 폴백) 조건:
     * - basis ∉ {FREIGHT_INPUT, TAX_ISSUED, SLIP_ISSUED}
     * - line-accel OFF
     * - hasBlNoFilter
     * - issued/grouped/documentStatus/taxType/documentNoLike/groupFinancialNo/operator/documentDtFrom/To 존재
     * - dateKind=="PERFORMANCE" && (dateFrom||dateTo): line-level → Mongo 처리
     * - needsLineGrainCorrelation (TAX/SLIP && (documentTypes||financialDocType) 동시)
     * - documentTypes && financialDocType 동시 존재
     * - perfDt 범위 한쪽만 존재 (open range)
     * - 일수 > maxDayBuckets
     * - perfDt 없고 FREIGHT_INPUT && 타입필터도 없는 형태
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

        // null 규칙 — 비정형·미지원 필터
        if (PmsMartFilterSupport.hasBlNoFilter(cmd)) return null;
        if (StringUtils.hasText(cmd.issued())) return null;
        if (StringUtils.hasText(cmd.grouped())) return null;
        if (StringUtils.hasText(cmd.documentStatus())) return null;
        if (StringUtils.hasText(cmd.taxType())) return null;
        if (StringUtils.hasText(cmd.documentNoLike())) return null;
        if (StringUtils.hasText(cmd.groupFinancialNo())) return null;
        if (StringUtils.hasText(cmd.operator())) return null;
        if (StringUtils.hasText(cmd.documentDtFrom()) || StringUtils.hasText(cmd.documentDtTo())) return null;

        // dateKind=="PERFORMANCE" + dateFrom/To: line-level 실적일자 → Mongo 처리
        if ("PERFORMANCE".equals(cmd.dateKind())
                && (StringUtils.hasText(cmd.dateFrom()) || StringUtils.hasText(cmd.dateTo()))) {
            return null;
        }

        // TAX/SLIP && (documentTypes || financialDocType) 동시 → needsLineGrainCorrelation → Mongo 폴백
        if (PmsMartFilterSupport.needsLineGrainCorrelation(cmd)) return null;

        // documentTypes && financialDocType 동시 존재 → 중복 타입 필터 → Mongo 폴백
        boolean hasDocTypes = cmd.documentTypes() != null && !cmd.documentTypes().isEmpty();
        boolean hasFdcType  = StringUtils.hasText(cmd.financialDocType());
        if (hasDocTypes && hasFdcType) return null;

        // perfDt 범위 처리
        String perfFrom = cmd.performanceDtFrom();
        String perfTo   = cmd.performanceDtTo();
        boolean hasPerfFrom = StringUtils.hasText(perfFrom);
        boolean hasPerfTo   = StringUtils.hasText(perfTo);

        if (hasPerfFrom || hasPerfTo) {
            // open range (한쪽만) → null
            if (hasPerfFrom != hasPerfTo) return null;

            // 일수 초과 검사
            long dayCount = countDays(perfFrom, perfTo);
            if (dayCount < 0) return null; // 파싱 실패
            if (dayCount > props.getCountIndex().getMaxDayBuckets()) {
                log.debug("Count Index freight: perfDt 일수({}) maxDayBuckets 초과 → Mongo 폴백", dayCount);
                return null;
            }

            return computeWithPerfDtRange(cmd, prefix, basis, perfFrom, perfTo, hasDocTypes, hasFdcType);
        }

        // perfDt 없는 경우: FREIGHT_INPUT && 타입 필터 있으면 전역 fdc 버킷 사용
        if (basis == AggregationBasis.FREIGHT_INPUT && (hasDocTypes || hasFdcType)) {
            return computeWithGlobalFdc(cmd, prefix, hasDocTypes, hasFdcType);
        }

        // perfDt 없고 타입 필터도 없는 형태 → Mongo 폴백
        return null;
    }

    // ── 내부 계산 ─────────────────────────────────────────────────────────────

    /**
     * perfDt 범위(양쪽 확정) + attr 기반 lineSet 구성 → dim AND → cardinality.
     */
    private Long computeWithPerfDtRange(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            String perfFrom,
            String perfTo,
            boolean hasDocTypes,
            boolean hasFdcType) {

        // attr 결정
        List<String> attrs = resolveAttrs(basis, cmd, hasDocTypes, hasFdcType);
        if (attrs == null) return null;

        // 일자 × 속성 키 수집
        List<String> lineKeys = buildPerfDtLineKeys(prefix, perfFrom, perfTo, attrs);
        if (lineKeys == null) return null; // 파싱 실패

        // ETD/ETA 범위 키(있으면)
        List<String> etdEtaKeys = collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null; // maxDistinctScan 초과

        // dim 키
        List<String> dimKeys = collectDimKeys(cmd, prefix);

        // 전체 키 목록: lineKeys + etdEtaKeys + dimKeys
        List<String> allKeys = new ArrayList<>(lineKeys.size() + etdEtaKeys.size() + dimKeys.size());
        allKeys.addAll(lineKeys);
        allKeys.addAll(etdEtaKeys);
        allKeys.addAll(dimKeys);

        if (allKeys.isEmpty()) return null;

        List<byte[]> allBytes = redisTemplate.opsForValue().multiGet(allKeys);
        if (allBytes == null) return null;

        Map<String, byte[]> keyToBytes = buildKeyMap(allKeys, allBytes);

        // lineKeys OR → lineSet
        RoaringBitmap lineSet = orBitmaps(lineKeys, keyToBytes);

        // etdEtaKeys OR → dateSet
        RoaringBitmap dateSet = orBitmaps(etdEtaKeys, keyToBytes);

        // 시작 집합: lineSet AND (dateSet있으면 AND)
        RoaringBitmap result = lineSet;
        if (result == null) return 0L;
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);

        // dim AND
        result = andDims(result, dimKeys, keyToBytes);

        return (long) result.getCardinality();
    }

    /**
     * perfDt 없고 FREIGHT_INPUT && 서류타입 필터가 있는 경우 전역 fdc 버킷 사용.
     */
    private Long computeWithGlobalFdc(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            boolean hasDocTypes,
            boolean hasFdcType) {

        List<String> fdcKeys = new ArrayList<>();
        if (hasDocTypes) {
            for (String t : cmd.documentTypes()) {
                if (StringUtils.hasText(t)) {
                    fdcKeys.add(PmsCountIndexKeys.lineGlobalFdcBitmap(prefix, t));
                }
            }
        } else if (hasFdcType) {
            fdcKeys.add(PmsCountIndexKeys.lineGlobalFdcBitmap(prefix, cmd.financialDocType()));
        }

        if (fdcKeys.isEmpty()) return null;

        List<String> etdEtaKeys = collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        List<String> dimKeys = collectDimKeys(cmd, prefix);

        List<String> allKeys = new ArrayList<>(fdcKeys.size() + etdEtaKeys.size() + dimKeys.size());
        allKeys.addAll(fdcKeys);
        allKeys.addAll(etdEtaKeys);
        allKeys.addAll(dimKeys);

        List<byte[]> allBytes = redisTemplate.opsForValue().multiGet(allKeys);
        if (allBytes == null) return null;

        Map<String, byte[]> keyToBytes = buildKeyMap(allKeys, allBytes);

        RoaringBitmap fdcSet  = orBitmaps(fdcKeys, keyToBytes);
        RoaringBitmap dateSet = orBitmaps(etdEtaKeys, keyToBytes);

        RoaringBitmap result = fdcSet;
        if (result == null) return 0L;
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);

        result = andDims(result, dimKeys, keyToBytes);
        return (long) result.getCardinality();
    }

    // ── 속성 결정 ─────────────────────────────────────────────────────────────

    /**
     * basis + 타입 필터에 따라 perfDt 일버킷에서 OR할 attr 목록을 반환한다.
     * null 반환은 지원 불가(Mongo 폴백 신호).
     */
    private List<String> resolveAttrs(
            AggregationBasis basis,
            SearchPmsPerformanceCommand cmd,
            boolean hasDocTypes,
            boolean hasFdcType) {

        return switch (basis) {
            case TAX_ISSUED  -> List.of(PmsCountIndexKeys.LINE_ATTR_HAS_TAX);
            case SLIP_ISSUED -> List.of(PmsCountIndexKeys.LINE_ATTR_HAS_SLIP);
            case FREIGHT_INPUT -> resolveFreightAttrs(cmd, hasDocTypes, hasFdcType);
            default -> null;
        };
    }

    private List<String> resolveFreightAttrs(
            SearchPmsPerformanceCommand cmd,
            boolean hasDocTypes,
            boolean hasFdcType) {

        if (!hasDocTypes && !hasFdcType) {
            return List.of(PmsCountIndexKeys.LINE_ATTR_HAS_FREIGHT);
        }
        List<String> attrs = new ArrayList<>();
        if (hasDocTypes) {
            for (String t : cmd.documentTypes()) {
                if (StringUtils.hasText(t)) attrs.add(PmsCountIndexKeys.LINE_FDC_PREFIX + t);
            }
        } else {
            attrs.add(PmsCountIndexKeys.LINE_FDC_PREFIX + cmd.financialDocType());
        }
        return attrs.isEmpty() ? null : attrs;
    }

    // ── 키 수집 헬퍼 ──────────────────────────────────────────────────────────

    /**
     * perfDt 범위 내 모든 일·속성 조합 키를 수집한다.
     * 파싱 실패 시 null 반환.
     */
    private List<String> buildPerfDtLineKeys(String prefix, String from, String to, List<String> attrs) {
        List<LocalDate> days = enumerateDays(from, to);
        if (days == null) return null;

        List<String> keys = new ArrayList<>(days.size() * attrs.size());
        for (LocalDate day : days) {
            String d = day.format(DATE_FMT);
            for (String attr : attrs) {
                keys.add(PmsCountIndexKeys.linePdAttrBitmap(prefix, d, attr));
            }
        }
        return keys;
    }

    /**
     * ETD/ETA 범위 키를 수집한다(있으면). maxDistinctScan 초과 시 null.
     */
    private List<String> collectEtdEtaKeys(SearchPmsPerformanceCommand cmd, String prefix) {
        String dateKind = cmd.dateKind();
        String dateFrom = cmd.dateFrom();
        String dateTo   = cmd.dateTo();

        boolean isEtd = "ETD".equals(dateKind) || !StringUtils.hasText(dateKind);
        boolean isEta = "ETA".equals(dateKind);

        if ((!isEtd && !isEta) || (!StringUtils.hasText(dateFrom) && !StringUtils.hasText(dateTo))) {
            return List.of();
        }

        String from = StringUtils.hasText(dateFrom) ? dateFrom : "00000000";
        String to   = StringUtils.hasText(dateTo)   ? dateTo   : "99999999";
        List<String> dayKeys = isEtd
            ? PmsCountIndexBitmapKeyCollector.etdDayKeys(prefix, from, to)
            : PmsCountIndexBitmapKeyCollector.etaDayKeys(prefix, from, to);

        if (dayKeys.size() > props.getCountIndex().getMaxDistinctScan()) {
            log.debug("Count Index freight: ETD/ETA 키 수({}) maxDistinctScan 초과 → Mongo 폴백", dayKeys.size());
            return null;
        }
        return dayKeys;
    }

    private List<String> collectDimKeys(SearchPmsPerformanceCommand cmd, String prefix) {
        List<String> keys = new ArrayList<>();
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, prefix, keys);
        return keys;
    }

    // ── 비트맵 연산 헬퍼 ─────────────────────────────────────────────────────

    private RoaringBitmap orBitmaps(List<String> keys, Map<String, byte[]> keyToBytes) {
        if (keys.isEmpty()) return null;
        RoaringBitmap result = new RoaringBitmap();
        for (String k : keys) {
            result.or(PmsCountIndexMaintainer.deserialize(keyToBytes.get(k)));
        }
        return result;
    }

    private RoaringBitmap andDims(RoaringBitmap base, List<String> dimKeys, Map<String, byte[]> keyToBytes) {
        RoaringBitmap result = base;
        for (String k : dimKeys) {
            RoaringBitmap dim = PmsCountIndexMaintainer.deserialize(keyToBytes.get(k));
            if (result == null) {
                result = dim.clone();
            } else {
                result = RoaringBitmap.and(result, dim);
            }
        }
        return result != null ? result : new RoaringBitmap();
    }

    private Map<String, byte[]> buildKeyMap(List<String> allKeys, List<byte[]> allBytes) {
        Map<String, byte[]> map = new HashMap<>(allKeys.size() * 2);
        for (int i = 0; i < allKeys.size(); i++) {
            map.put(allKeys.get(i), allBytes.get(i));
        }
        return map;
    }

    // ── 날짜 유틸 ─────────────────────────────────────────────────────────────

    /**
     * yyyyMMdd 문자열 두 개로 from~to 사이 일수를 반환한다.
     * 파싱 실패 시 -1.
     */
    private long countDays(String from, String to) {
        try {
            LocalDate start = LocalDate.parse(from, DATE_FMT);
            LocalDate end   = LocalDate.parse(to, DATE_FMT);
            if (end.isBefore(start)) return 0;
            return end.toEpochDay() - start.toEpochDay() + 1;
        } catch (DateTimeParseException e) {
            return -1;
        }
    }

    /**
     * from~to 사이 모든 LocalDate를 열거한다.
     * 파싱 실패 시 null.
     */
    private List<LocalDate> enumerateDays(String from, String to) {
        try {
            LocalDate start = LocalDate.parse(from, DATE_FMT);
            LocalDate end   = LocalDate.parse(to, DATE_FMT);
            List<LocalDate> days = new ArrayList<>();
            LocalDate cur = start;
            while (!cur.isAfter(end)) {
                days.add(cur);
                cur = cur.plusDays(1);
            }
            return days;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
