package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase C document 경로 판정·계산 협력자 (package-private).
 *
 * PmsRedisExactCountProvider에서 위임받아 docs[] fdId-grain 비트맵 기반
 * DOCUMENT_CREATED basis의 정확 count를 계산한다.
 *
 * <p>PmsMartPageCriteriaBuilder.buildDocumentElemMatch 분석 결과:
 * <ul>
 *   <li>perfPd, docDt, docType, status, grouped, team, operator 모두 docs[] $elemMatch 내부 → same-doc 상관 보장.
 *   <li>teamCode → docs[] 원소의 team 필드 (doc-level). 반면 fast-path(PmsMartCriteriaBuilder.buildDocument)는
 *       documentCreated.teamCode(B/L max). 이 경로는 page 경로(line-accel) 의미를 미러하므로 dc:team:{code}(fdId-grain) 사용.
 *   <li>operator → docs[] 원소의 operator 필드 (doc-level) → dc:op:{op}(fdId-grain).
 *   <li>grouped "Y"→grouped=true / "N"→grouped=false (ANDNOT dc:grouped 사용).
 * </ul>
 *
 * <p>null 규칙(Mongo 폴백):
 * <ul>
 *   <li>basis != DOCUMENT_CREATED
 *   <li>line-accel OFF
 *   <li>dc:overflow 플래그 존재
 *   <li>hasBlNoFilter
 *   <li>issued, financialDocType, taxType, documentNoLike, groupFinancialNo 존재
 *   <li>dateKind=="PERFORMANCE" && (dateFrom || dateTo)
 *   <li>performanceDtFrom/To 한쪽만 (open range) → 불확실(날짜 버킷 범위 미확정)
 *   <li>documentDtFrom/To 한쪽만 (open range)
 *   <li>일수 > maxDayBuckets
 *   <li>doc-level 술어(날짜 포함)가 하나도 없는 형태 → null(fast-path 소관)
 *   <li>매칭 fdId cardinality > maxDistinctScan (collapse 비용 상한)
 * </ul>
 */
@Slf4j
final class PmsCountIndexDocumentPath {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final PmsMartProperties props;

    PmsCountIndexDocumentPath(RedisTemplate<String, byte[]> redisTemplate, PmsMartProperties props) {
        this.redisTemplate = redisTemplate;
        this.props = props;
    }

    // ── 공개(package-private) API ─────────────────────────────────────────────

    /**
     * document basis 경로 계산을 시도한다.
     * null 반환은 Mongo 폴백 신호.
     */
    Long computeDocumentCount(SearchPmsPerformanceCommand cmd, String prefix) {
        // basis 확인
        if (cmd.effectiveBasis() != AggregationBasis.DOCUMENT_CREATED) return null;

        // line-accel OFF: dc:* 버킷 미적재 → 폴백
        if (!props.getLineAccel().isEnabled()) return null;

        // dc:overflow 플래그 → 적재 불완전 → 폴백
        byte[] overflowFlag = redisTemplate.opsForValue().get(PmsCountIndexKeys.docOverflowFlag(prefix));
        if (overflowFlag != null) {
            log.debug("Count Index document: dc:overflow 플래그 존재 → Mongo 폴백");
            return null;
        }

        // null 규칙 — 비정형·미지원 필터
        if (PmsMartFilterSupport.hasBlNoFilter(cmd)) return null;
        if (StringUtils.hasText(cmd.issued())) return null;
        if (StringUtils.hasText(cmd.financialDocType())) return null;
        if (StringUtils.hasText(cmd.taxType())) return null;
        if (StringUtils.hasText(cmd.documentNoLike())) return null;
        if (StringUtils.hasText(cmd.groupFinancialNo())) return null;

        // dateKind=="PERFORMANCE" + 날짜: line-level → 폴백
        if ("PERFORMANCE".equals(cmd.dateKind())
                && (StringUtils.hasText(cmd.dateFrom()) || StringUtils.hasText(cmd.dateTo()))) {
            return null;
        }

        // performanceDt open range 검사
        String perfFrom = cmd.performanceDtFrom();
        String perfTo   = cmd.performanceDtTo();
        boolean hasPerfFrom = StringUtils.hasText(perfFrom);
        boolean hasPerfTo   = StringUtils.hasText(perfTo);
        if (hasPerfFrom != hasPerfTo) return null; // open range

        // documentDt open range 검사
        String docFrom = cmd.documentDtFrom();
        String docTo   = cmd.documentDtTo();
        boolean hasDocFrom = StringUtils.hasText(docFrom);
        boolean hasDocTo   = StringUtils.hasText(docTo);
        if (hasDocFrom != hasDocTo) return null; // open range

        // 일수 초과 검사 (각각)
        if (hasPerfFrom) {
            long days = countDays(perfFrom, perfTo);
            if (days < 0) return null;
            if (days > props.getCountIndex().getMaxDayBuckets()) {
                log.debug("Count Index document: perfDt 일수({}) maxDayBuckets 초과 → Mongo 폴백", days);
                return null;
            }
        }
        if (hasDocFrom) {
            long days = countDays(docFrom, docTo);
            if (days < 0) return null;
            if (days > props.getCountIndex().getMaxDayBuckets()) {
                log.debug("Count Index document: docDt 일수({}) maxDayBuckets 초과 → Mongo 폴백", days);
                return null;
            }
        }

        // doc-level 술어가 하나도 없으면 fast-path 소관 → null
        boolean hasAnyDocPredicate = hasPerfFrom || hasDocFrom
            || (cmd.documentTypes() != null && !cmd.documentTypes().isEmpty())
            || StringUtils.hasText(cmd.documentStatus())
            || StringUtils.hasText(cmd.grouped())
            || StringUtils.hasText(cmd.teamCode())
            || StringUtils.hasText(cmd.operator());
        if (!hasAnyDocPredicate) return null;

        return doComputeDocumentCount(cmd, prefix, perfFrom, perfTo, hasPerfFrom,
                                      docFrom, docTo, hasDocFrom);
    }

    // ── 내부 계산 ─────────────────────────────────────────────────────────────

    private Long doComputeDocumentCount(
            SearchPmsPerformanceCommand cmd,
            String prefix,
            String perfFrom, String perfTo, boolean hasPerfFrom,
            String docFrom, String docTo, boolean hasDocFrom) {

        // 1. base fdId 비트맵 계산
        RoaringBitmap base = computeBaseFdIdBitmap(prefix, perfFrom, perfTo, hasPerfFrom,
                                                   docFrom, docTo, hasDocFrom);
        if (base == null) return null; // 파싱 실패

        // 2. AND: documentTypes
        List<String> types = cmd.documentTypes();
        if (types != null && !types.isEmpty()) {
            RoaringBitmap typeUnion = new RoaringBitmap();
            for (String t : types) {
                if (StringUtils.hasText(t)) {
                    byte[] b = fetchBitmap(PmsCountIndexKeys.docTypeBitmap(prefix, t));
                    typeUnion.or(PmsCountIndexMaintainer.deserialize(b));
                }
            }
            base = RoaringBitmap.and(base, typeUnion);
        }

        // 3. AND: documentStatus
        if (StringUtils.hasText(cmd.documentStatus())) {
            byte[] b = fetchBitmap(PmsCountIndexKeys.docStatusBitmap(prefix, cmd.documentStatus()));
            base = RoaringBitmap.and(base, PmsCountIndexMaintainer.deserialize(b));
        }

        // 4. grouped Y→AND / N→ANDNOT
        if (StringUtils.hasText(cmd.grouped())) {
            switch (cmd.grouped()) {
                case "Y" -> {
                    byte[] b = fetchBitmap(PmsCountIndexKeys.docGroupedBitmap(prefix));
                    base = RoaringBitmap.and(base, PmsCountIndexMaintainer.deserialize(b));
                }
                case "N" -> {
                    byte[] b = fetchBitmap(PmsCountIndexKeys.docGroupedBitmap(prefix));
                    base = RoaringBitmap.andNot(base, PmsCountIndexMaintainer.deserialize(b));
                }
                default -> { /* 미인식: 무시 */ }
            }
        }

        // 5. teamCode → dc:team (docs[] 원소 레벨 — page 경로 의미 미러)
        if (StringUtils.hasText(cmd.teamCode())) {
            byte[] b = fetchBitmap(PmsCountIndexKeys.docTeamBitmap(prefix, cmd.teamCode()));
            base = RoaringBitmap.and(base, PmsCountIndexMaintainer.deserialize(b));
        }

        // 6. operator → dc:op
        if (StringUtils.hasText(cmd.operator())) {
            byte[] b = fetchBitmap(PmsCountIndexKeys.docOperatorBitmap(prefix, cmd.operator()));
            base = RoaringBitmap.and(base, PmsCountIndexMaintainer.deserialize(b));
        }

        // 7. cardinality > maxDistinctScan → collapse 비용 상한 → null
        long fdIdCardinality = base.getLongCardinality();
        if (fdIdCardinality == 0) return 0L;
        if (fdIdCardinality > props.getCountIndex().getMaxDistinctScan()) {
            log.debug("Count Index document: fdId cardinality({}) maxDistinctScan 초과 → Mongo 폴백", fdIdCardinality);
            return null;
        }

        // 8. fdId → blOrdinal collapse: dc:bl HMGET (10,000개 chunk)
        RoaringBitmap blOrdinalBitmap = collapseToBlOrdinals(base, prefix);

        // 9. B/L 차원 AND (ETD/ETA + dim keys from FreightPath 공용 헬퍼)
        blOrdinalBitmap = andBlDimBitmaps(blOrdinalBitmap, cmd, prefix);
        if (blOrdinalBitmap == null) return null;

        return blOrdinalBitmap.getLongCardinality();
    }

    /**
     * perfDt/docDt 범위로 base fdId 비트맵을 계산한다.
     * 둘 다 없으면 dc:all(전체).
     * 둘 다 있으면 각각 일버킷 OR 후 AND.
     */
    private RoaringBitmap computeBaseFdIdBitmap(
            String prefix,
            String perfFrom, String perfTo, boolean hasPerfFrom,
            String docFrom, String docTo, boolean hasDocFrom) {

        RoaringBitmap perfBase = null;
        if (hasPerfFrom) {
            List<LocalDate> days = enumerateDays(perfFrom, perfTo);
            if (days == null) return null;
            perfBase = new RoaringBitmap();
            for (LocalDate day : days) {
                String d = day.format(DATE_FMT);
                byte[] b = fetchBitmap(PmsCountIndexKeys.docPerfPdDayBitmap(prefix, d));
                perfBase.or(PmsCountIndexMaintainer.deserialize(b));
            }
        }

        RoaringBitmap docBase = null;
        if (hasDocFrom) {
            List<LocalDate> days = enumerateDays(docFrom, docTo);
            if (days == null) return null;
            docBase = new RoaringBitmap();
            for (LocalDate day : days) {
                String d = day.format(DATE_FMT);
                byte[] b = fetchBitmap(PmsCountIndexKeys.docDtDayBitmap(prefix, d));
                docBase.or(PmsCountIndexMaintainer.deserialize(b));
            }
        }

        if (perfBase != null && docBase != null) {
            return RoaringBitmap.and(perfBase, docBase);
        }
        if (perfBase != null) return perfBase;
        if (docBase != null) return docBase;

        // 날짜 없음 → dc:all
        byte[] allBytes = fetchBitmap(PmsCountIndexKeys.docAllBitmap(prefix));
        return PmsCountIndexMaintainer.deserialize(allBytes);
    }

    /**
     * fdId RoaringBitmap을 dc:bl 해시 HMGET으로 blOrdinal들로 변환한다.
     * 10,000개 chunk pipelined.
     * 매핑 누락 fdId는 skip + warn(1회).
     */
    private RoaringBitmap collapseToBlOrdinals(RoaringBitmap fdIdBitmap, String prefix) {
        String collapseHashKey = PmsCountIndexKeys.docCollapseHash(prefix);
        int[] fdIds = fdIdBitmap.toArray();
        RoaringBitmap blOrdinals = new RoaringBitmap();
        boolean warnedMissing = false;
        int chunkSize = 10_000;

        for (int start = 0; start < fdIds.length; start += chunkSize) {
            int end = Math.min(start + chunkSize, fdIds.length);
            List<Object> fields = new ArrayList<>(end - start);
            for (int i = start; i < end; i++) {
                fields.add(String.valueOf(fdIds[i]));
            }
            List<Object> vals = redisTemplate.opsForHash().multiGet(collapseHashKey, fields);
            if (vals == null) continue;
            for (int i = 0; i < vals.size(); i++) {
                Object val = vals.get(i);
                if (val == null) {
                    if (!warnedMissing) {
                        warnedMissing = true;
                        log.warn("Count Index document collapse: fdId {} dc:bl 매핑 누락 — skip", fdIds[start + i]);
                    }
                    continue;
                }
                String s = (val instanceof byte[]) ? new String((byte[]) val, StandardCharsets.UTF_8) : val.toString();
                blOrdinals.add(Integer.parseInt(s));
            }
        }
        return blOrdinals;
    }

    /**
     * blOrdinal 비트맵에 ETD/ETA 일버킷 AND + dim 키 AND를 적용한다.
     * PmsCountIndexFreightPath와 동일한 ETD/ETA 처리 패턴.
     * maxDistinctScan 초과 시 null.
     */
    private RoaringBitmap andBlDimBitmaps(
            RoaringBitmap blOrdinals,
            SearchPmsPerformanceCommand cmd,
            String prefix) {

        // ETD/ETA 일버킷 OR
        List<String> etdEtaKeys = collectEtdEtaKeys(cmd, prefix);
        if (etdEtaKeys == null) return null; // maxDistinctScan 초과

        // dim 키 (B/L-level 차원)
        List<String> dimKeys = new ArrayList<>();
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, prefix, dimKeys);

        // 모든 키 MGET
        List<String> allKeys = new ArrayList<>(etdEtaKeys.size() + dimKeys.size());
        allKeys.addAll(etdEtaKeys);
        allKeys.addAll(dimKeys);

        if (allKeys.isEmpty()) return blOrdinals;

        List<byte[]> allBytes = redisTemplate.opsForValue().multiGet(allKeys);
        if (allBytes == null) return blOrdinals;

        Map<String, byte[]> keyToBytes = new HashMap<>(allKeys.size() * 2);
        for (int i = 0; i < allKeys.size(); i++) {
            keyToBytes.put(allKeys.get(i), allBytes.get(i));
        }

        // ETD/ETA OR
        RoaringBitmap result = blOrdinals;
        if (!etdEtaKeys.isEmpty()) {
            RoaringBitmap dateSet = new RoaringBitmap();
            for (String k : etdEtaKeys) {
                dateSet.or(PmsCountIndexMaintainer.deserialize(keyToBytes.get(k)));
            }
            result = RoaringBitmap.and(result, dateSet);
        }

        // dim AND
        for (String k : dimKeys) {
            RoaringBitmap dim = PmsCountIndexMaintainer.deserialize(keyToBytes.get(k));
            result = RoaringBitmap.and(result, dim);
        }

        return result;
    }

    // ── 키 수집 헬퍼 ──────────────────────────────────────────────────────────

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
            log.debug("Count Index document: ETD/ETA 키 수({}) maxDistinctScan 초과 → Mongo 폴백", dayKeys.size());
            return null;
        }
        return dayKeys;
    }

    // ── 비트맵 fetch 헬퍼 ─────────────────────────────────────────────────────

    private byte[] fetchBitmap(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // ── 날짜 유틸 ─────────────────────────────────────────────────────────────

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
