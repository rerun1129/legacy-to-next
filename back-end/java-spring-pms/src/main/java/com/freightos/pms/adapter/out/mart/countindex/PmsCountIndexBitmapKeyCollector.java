package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Count Index 쿼리 경로에서 비트맵 키 목록을 수집하는 순수 헬퍼.
 *
 * dim 필터 키 수집과 ETD/ETA 날짜 범위 일별 버킷 키 확장을 담당한다.
 * Spring 빈이 아닌 package-private 유틸 클래스.
 */
final class PmsCountIndexBitmapKeyCollector {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private PmsCountIndexBitmapKeyCollector() {}

    /**
     * SearchPmsPerformanceCommand의 차원 필터를 비트맵 키 목록으로 변환해 결과에 추가한다.
     *
     * 각 dim 코드는 독립 AND 교집합 대상이다.
     * polCode/podCode 통합 필드(portKind/portCode)도 처리한다.
     */
    static void collectDimKeys(SearchPmsPerformanceCommand cmd, String prefix, List<String> keys) {
        addDim(keys, prefix, PmsCountIndexKeys.DIM_CUST,       cmd.actualCustomerCode());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_SPC,        cmd.settlePartnerCode());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_LINER,      cmd.carrierCode());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_SALESMAN,   cmd.salesManCode());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_SALESCLASS, cmd.salesClass());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_INCOTERMS,  cmd.incoterms());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_JOBDIV,     cmd.jobDiv());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_BOUND,      cmd.bound());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_HOUSETEAM,  cmd.teamCode());

        // partyKind/partyCode: 둘 다 hasText일 때만. case-sensitive 정확 매칭.
        // PmsMartCriteriaBuilder.addPartyFilter(:126-131) 미러.
        if (StringUtils.hasText(cmd.partyKind()) && StringUtils.hasText(cmd.partyCode())) {
            switch (cmd.partyKind()) {
                case "ACTUAL_CUSTOMER" ->
                    keys.add(PmsCountIndexKeys.dimBitmap(prefix, PmsCountIndexKeys.DIM_CUST, cmd.partyCode()));
                case "SETTLE_PARTNER" ->
                    keys.add(PmsCountIndexKeys.dimBitmap(prefix, PmsCountIndexKeys.DIM_SPC, cmd.partyCode()));
                default -> { /* 미인식 partyKind: Mongo도 무시하므로 Redis도 무시 */ }
            }
        }

        // portKind/portCode: 둘 다 hasText일 때만. case-sensitive 정확 매칭.
        // PmsMartCriteriaBuilder.addPortFilter 미러 — equalsIgnoreCase 금지.
        if (StringUtils.hasText(cmd.portKind()) && StringUtils.hasText(cmd.portCode())) {
            switch (cmd.portKind()) {
                case "POL" ->
                    keys.add(PmsCountIndexKeys.dimBitmap(prefix, PmsCountIndexKeys.DIM_POL, cmd.portCode()));
                case "POD" ->
                    keys.add(PmsCountIndexKeys.dimBitmap(prefix, PmsCountIndexKeys.DIM_POD, cmd.portCode()));
                default -> { /* 미인식 portKind: Mongo도 무시하므로 Redis도 무시 */ }
            }
        }
    }

    /**
     * ETD 날짜 범위(yyyyMMdd 문자열)에 해당하는 일별 버킷 키 목록을 생성한다.
     *
     * from~to 구간의 모든 일자를 열거한다(범위가 클 경우 호출측에서 maxDistinctScan 검사 필요).
     */
    static List<String> etdDayKeys(String prefix, String from, String to) {
        return buildDayKeys(prefix, from, to, true);
    }

    /**
     * ETA 날짜 범위(yyyyMMdd 문자열)에 해당하는 일별 버킷 키 목록을 생성한다.
     */
    static List<String> etaDayKeys(String prefix, String from, String to) {
        return buildDayKeys(prefix, from, to, false);
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────────

    private static void addDim(List<String> keys, String prefix, String dim, String code) {
        if (StringUtils.hasText(code)) {
            keys.add(PmsCountIndexKeys.dimBitmap(prefix, dim, code));
        }
    }

    private static List<String> buildDayKeys(String prefix, String from, String to, boolean isEtd) {
        List<String> keys = new ArrayList<>();
        try {
            LocalDate start = LocalDate.parse(from, DATE_FMT);
            LocalDate end   = LocalDate.parse(to,   DATE_FMT);
            LocalDate cur   = start;
            while (!cur.isAfter(end)) {
                String day = cur.format(DATE_FMT);
                keys.add(isEtd
                    ? PmsCountIndexKeys.etdDayBitmap(prefix, day)
                    : PmsCountIndexKeys.etaDayBitmap(prefix, day));
                cur = cur.plusDays(1);
            }
        } catch (Exception e) {
            // 날짜 파싱 실패 시 빈 반환 → 호출측에서 Mongo 폴백
        }
        return keys;
    }
}
