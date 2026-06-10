package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;

import java.util.List;

/**
 * SearchPmsPerformanceCommand 에서 조회 결과에 영향을 주는 필터 필드만 추출해
 * 캐시 키용 서명 문자열을 생성하는 유틸.
 *
 * page/size/exactCount 는 캐시 대상이 아니므로 제외한다.
 * null 값은 빈 문자열로 안정(stable) 직렬화한다.
 * documentTypes 리스트는 정렬 후 join 하여 순서 무관 동등성을 보장한다.
 */
final class PmsPerformanceFilterSignature {

    private PmsPerformanceFilterSignature() {}

    /**
     * 필터 서명 문자열을 반환한다.
     * 반환값은 캐시 키의 일부로 사용되며 human-readable 포맷을 보장하지 않는다.
     */
    static String of(SearchPmsPerformanceCommand c) {
        return s(c.effectiveBasis().name())
            + "|jd=" + s(c.jobDiv())
            + "|bd=" + s(c.bound())
            + "|dk=" + s(c.dateKind())
            + "|df=" + s(c.dateFrom())
            + "|dt=" + s(c.dateTo())
            + "|pf=" + s(c.performanceDtFrom())
            + "|pt=" + s(c.performanceDtTo())
            + "|ddf=" + s(c.documentDtFrom())
            + "|ddt=" + s(c.documentDtTo())
            + "|dt2=" + sortedJoin(c.documentTypes())
            + "|ds=" + s(c.documentStatus())
            + "|gr=" + s(c.grouped())
            + "|is=" + s(c.issued())
            + "|sn=" + (c.searchNonce() != null ? c.searchNonce() : "");
    }

    private static String s(String v) {
        return v != null ? v : "";
    }

    private static String sortedJoin(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().sorted().reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
    }
}
