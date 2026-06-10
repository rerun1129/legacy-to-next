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
 *
 * W1-A: FE가 전송하지 않는 차원(actualCustomerCode/settlePartnerCode/carrierCode/
 *        salesManCode/salesClass/incoterms/teamCode/partyKind/partyCode/portKind/portCode)
 *        제거. jobDiv/bound만 잔존.
 */
final class PmsCountIndexBitmapKeyCollector {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private PmsCountIndexBitmapKeyCollector() {}

    /**
     * SearchPmsPerformanceCommand의 차원 필터를 비트맵 키 목록으로 변환해 결과에 추가한다.
     * FE가 전송하는 jobDiv/bound 두 차원만 처리한다.
     */
    static void collectDimKeys(SearchPmsPerformanceCommand cmd, String prefix, List<String> keys) {
        addDim(keys, prefix, PmsCountIndexKeys.DIM_JOBDIV, cmd.jobDiv());
        addDim(keys, prefix, PmsCountIndexKeys.DIM_BOUND,  cmd.bound());
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
