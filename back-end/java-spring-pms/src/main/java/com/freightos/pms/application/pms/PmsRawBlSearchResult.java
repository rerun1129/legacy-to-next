package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import org.springframework.data.domain.Page;

/**
 * 아웃바운드 포트(PmsPerformanceQueryPort) 반환 타입.
 *
 * Spring {@link Page}에 approximateTotal 플래그를 더한 래퍼.
 * - OLTP 어댑터: 항상 approximateTotal=false (정확 count).
 * - Mart 어댑터: $sample 근사 추정 분기이면 approximateTotal=true, 그 외 false.
 */
public record PmsRawBlSearchResult(
    Page<PmsRawBlRow> page,
    boolean approximateTotal
) {

    /** OLTP / 정확 count 경로 전용 편의 팩토리. */
    public static PmsRawBlSearchResult exact(Page<PmsRawBlRow> page) {
        return new PmsRawBlSearchResult(page, false);
    }
}
