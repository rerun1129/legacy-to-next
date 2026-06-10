package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
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
 * PmsCountIndexFreightPath 내부 헬퍼 모음 (package-private).
 *
 * 500줄 초과 방지를 위해 PmsCountIndexFreightPath에서 분리된 유틸 클래스.
 * - E3 composite 키 계산(computeWithComposite / addCompositeKeysForBasis)
 * - 키 수집(perfDt 일버킷·ETD/ETA·dim)
 * - 비트맵 연산(OR/AND)
 * - 날짜 유틸(countDays/enumerateDays)
 *
 * Spring 빈이 아닌 package-private 유틸 클래스.
 */
@Slf4j
final class PmsCountIndexFreightPathSupport {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final PmsMartProperties props;

    PmsCountIndexFreightPathSupport(RedisTemplate<String, byte[]> redisTemplate, PmsMartProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    // ── 키 수집 ───────────────────────────────────────────────────────────────

    /**
     * perfDt 범위 내 모든 일·속성 조합 키를 수집한다.
     * 파싱 실패 시 null 반환.
     */
    List<String> buildPerfDtLineKeys(String prefix, String from, String to, List<String> attrs) {
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
    List<String> collectEtdEtaKeys(SearchPmsPerformanceCommand cmd, String prefix) {
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

    List<String> collectDimKeys(SearchPmsPerformanceCommand cmd, String prefix) {
        List<String> keys = new ArrayList<>();
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, prefix, keys);
        return keys;
    }

    // ── 비트맵 연산 ───────────────────────────────────────────────────────────

    RoaringBitmap orBitmaps(List<String> keys, Map<String, byte[]> keyToBytes) {
        if (keys.isEmpty()) return null;
        RoaringBitmap result = new RoaringBitmap();
        for (String k : keys) {
            result.or(PmsCountIndexMaintainer.deserialize(keyToBytes.get(k)));
        }
        return result;
    }

    RoaringBitmap andDims(RoaringBitmap base, List<String> dimKeys, Map<String, byte[]> keyToBytes) {
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

    Map<String, byte[]> buildKeyMap(List<String> allKeys, List<byte[]> allBytes) {
        Map<String, byte[]> map = new HashMap<>(allKeys.size() * 2);
        for (int i = 0; i < allKeys.size(); i++) {
            map.put(allKeys.get(i), allBytes.get(i));
        }
        return map;
    }

    /**
     * Redis에서 allKeys를 일괄 조회하고 키-바이트 맵을 반환한다.
     * opsForValue().multiGet() 응답이 null이면 null 반환.
     */
    Map<String, byte[]> multiGetAsMap(List<String> allKeys) {
        if (allKeys.isEmpty()) return Map.of();
        List<byte[]> allBytes = redisTemplate.opsForValue().multiGet(allKeys);
        if (allBytes == null) return null;
        return buildKeyMap(allKeys, allBytes);
    }

    // ── E3 composite 계산 (W2) ───────────────────────────────────────────────

    /**
     * E3 composite 버킷 기반 계산.
     *
     * basis + fdcType 조합에 맞는 2-bit composite 키를 열거해 OR → dim AND → cardinality.
     *
     * @return cardinality, 또는 지원 불가 시 null
     */
    Long computeWithComposite(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            String perfFrom,
            String perfTo,
            boolean hasDocTypes) {

        RoaringBitmap bitmap = computeWithCompositeBitmap(cmd, prefix, basis, perfFrom, perfTo, hasDocTypes);
        if (bitmap == null) return null;
        return (long) bitmap.getCardinality();
    }

    /**
     * E3 composite 일버킷 기반 계산 — B/L ordinal 비트맵 반환.
     *
     * 2-bit(t/s) 조합 × fdcType 변형 × 일수.
     * E2 doc-grain AND 합성을 위해 PmsCountIndexFreightPath에서 호출한다.
     * null 반환은 지원 불가(Mongo 폴백 신호).
     */
    RoaringBitmap computeWithCompositeBitmap(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            String perfFrom,
            String perfTo,
            boolean hasDocTypes) {

        List<LocalDate> days = enumerateDays(perfFrom, perfTo);
        if (days == null) return null;

        // documentTypes 미지정(무제약) = 모든 fdcType 변형 열거.
        // FDC_ALL_TYPES 마지막 원소 null → encodeType(null)="none" 버킷(fdcType 없는 라인) 포함.
        List<String> typeList = hasDocTypes ? cmd.documentTypes() : PmsCountIndexKeys.FDC_ALL_TYPES;
        List<String> compositeKeys = new ArrayList<>();

        for (LocalDate day : days) {
            String d = day.format(DATE_FMT);
            for (String fdcType : typeList) {
                addCompositeKeysForBasis(compositeKeys, prefix, d, basis, fdcType);
            }
        }

        if (compositeKeys.isEmpty()) return null;

        if (compositeKeys.size() > props.getCountIndex().getMaxDistinctScan() * 2) {
            log.debug("Count Index freight composite: 키 수({}) 초과 → Mongo 폴백", compositeKeys.size());
            return null;
        }

        List<String> etdEtaKeys = collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        List<String> dimKeys = collectDimKeys(cmd, prefix);

        List<String> allKeys = buildAllKeysList(compositeKeys, etdEtaKeys, dimKeys);
        Map<String, byte[]> keyToBytes = multiGetAsMap(allKeys);
        if (keyToBytes == null) return null;

        RoaringBitmap lineSet = orBitmaps(compositeKeys, keyToBytes);
        RoaringBitmap dateSet = orBitmaps(etdEtaKeys, keyToBytes);

        RoaringBitmap result = lineSet;
        if (result == null) return new RoaringBitmap();
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);
        result = andDims(result, dimKeys, keyToBytes);

        return result;
    }

    /**
     * basis에 따라 composite 일버킷 키를 수집한다(2-bit t/s).
     */
    void addCompositeKeysForBasis(
            List<String> keys, String prefix, String day,
            AggregationBasis basis, String fdcType) {
        switch (basis) {
            case FREIGHT_INPUT -> {
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, false, false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, true,  false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, false, true,  fdcType));
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, true,  true,  fdcType));
            }
            case TAX_ISSUED -> {
                // t=1 고정, s 열거
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, true, false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, true, true,  fdcType));
            }
            case SLIP_ISSUED -> {
                // s=1 고정, t 열거
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, false, true, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositePdBitmap(prefix, day, true,  true, fdcType));
            }
            default -> { /* no-op */ }
        }
    }

    List<String> buildAllKeysList(List<String> a, List<String> b, List<String> c) {
        List<String> all = new ArrayList<>(a.size() + b.size() + c.size());
        all.addAll(a); all.addAll(b); all.addAll(c);
        return all;
    }

    // ── E3 전역 composite 계산 (perfDt 없음) ─────────────────────────────────

    /**
     * perfDt 없는 케이스: 전역 composite 버킷({p}:ln:c:{t}{s}:{TYPE}) 기반 계산.
     *
     * 2-bit(t/s) × fdcType 변형 열거. basis에 따라 t/s 고정 여부 다름.
     * 변형 수(compositeKeys.size()) > maxDayBuckets×8 이면 null(키 수 가드).
     *
     * @return B/L ordinal 비트맵, 지원 불가 시 null
     */
    RoaringBitmap computeWithCompositeGlobalBitmap(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            AggregationBasis basis,
            boolean hasDocTypes) {

        // documentTypes 미지정(무제약) = 모든 fdcType 변형 열거.
        // FDC_ALL_TYPES 마지막 원소 null → encodeType(null)="none" 버킷(fdcType 없는 라인) 포함.
        List<String> typeList = hasDocTypes ? cmd.documentTypes() : PmsCountIndexKeys.FDC_ALL_TYPES;
        List<String> compositeKeys = new ArrayList<>();

        for (String fdcType : typeList) {
            addCompositeGlobalKeysForBasis(compositeKeys, prefix, basis, fdcType);
        }

        if (compositeKeys.isEmpty()) return null;

        // 변형 수 가드: maxDayBuckets×8
        long keyGuard = (long) props.getCountIndex().getMaxDayBuckets() * 8;
        if (compositeKeys.size() > keyGuard) {
            log.debug("Count Index freight global composite: 키 수({}) 가드 초과 → Mongo 폴백", compositeKeys.size());
            return null;
        }

        List<String> etdEtaKeys = collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null;

        List<String> dimKeys = collectDimKeys(cmd, prefix);

        List<String> allKeys = buildAllKeysList(compositeKeys, etdEtaKeys, dimKeys);
        Map<String, byte[]> keyToBytes = multiGetAsMap(allKeys);
        if (keyToBytes == null) return null;

        RoaringBitmap lineSet = orBitmaps(compositeKeys, keyToBytes);
        RoaringBitmap dateSet = orBitmaps(etdEtaKeys, keyToBytes);

        RoaringBitmap result = lineSet;
        if (result == null) return new RoaringBitmap();
        if (dateSet != null) result = RoaringBitmap.and(result, dateSet);
        return andDims(result, dimKeys, keyToBytes);
    }

    /**
     * basis에 따라 전역 composite 키를 수집한다(2-bit t/s).
     * 일버킷과 달리 day 파라미터 없음 — lineCompositeGlobalBitmap 사용.
     */
    void addCompositeGlobalKeysForBasis(
            List<String> keys, String prefix,
            AggregationBasis basis, String fdcType) {
        switch (basis) {
            case FREIGHT_INPUT -> {
                // t,s 모두 열거
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, false, false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, true,  false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, false, true,  fdcType));
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, true,  true,  fdcType));
            }
            case TAX_ISSUED -> {
                // t=1 고정, s 열거
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, true, false, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, true, true,  fdcType));
            }
            case SLIP_ISSUED -> {
                // s=1 고정, t 열거
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, false, true, fdcType));
                keys.add(PmsCountIndexKeys.lineCompositeGlobalBitmap(prefix, true,  true, fdcType));
            }
            default -> { /* no-op */ }
        }
    }

    // ── 날짜 유틸 ─────────────────────────────────────────────────────────────

    /**
     * yyyyMMdd 문자열 두 개로 from~to 사이 일수를 반환한다.
     * 파싱 실패 시 -1.
     */
    long countDays(String from, String to) {
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
    List<LocalDate> enumerateDays(String from, String to) {
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
