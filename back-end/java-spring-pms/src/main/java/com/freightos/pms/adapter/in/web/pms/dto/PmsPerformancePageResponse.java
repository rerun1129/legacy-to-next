package com.freightos.pms.adapter.in.web.pms.dto;

import java.util.List;

/**
 * POST /api/pms/performance/search 페이지 응답 DTO.
 *
 * approximateTotal=true이면 totalElements가 $sample 기반 근사 추정치임을 의미한다.
 * FE는 이 플래그가 true일 때 총건수 표시에 "약 N건" 같은 근사 표기를 적용할 수 있다.
 */
public record PmsPerformancePageResponse(
    List<PmsPerformanceRowResponse> content,
    long totalElements,
    int totalPages,
    int page,
    int size,
    boolean approximateTotal
) {}
