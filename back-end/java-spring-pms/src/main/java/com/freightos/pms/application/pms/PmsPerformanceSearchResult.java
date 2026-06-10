package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.projection.PmsPerformanceRowView;
import org.springframework.data.domain.Page;

/**
 * PMS 실적 조회 결과 래퍼.
 *
 * Spring {@link Page}는 totalElements가 근사치인지 여부를 실을 수 없으므로,
 * 페이지 데이터와 approximateTotal 플래그를 함께 담는 application 계층 결과 타입.
 *
 * approximateTotal=true: totalElements가 $sample 기반 근사 추정치임.
 * approximateTotal=false: totalElements가 정확한 count임.
 */
public record PmsPerformanceSearchResult(
    Page<PmsPerformanceRowView> page,
    boolean approximateTotal
) {}
