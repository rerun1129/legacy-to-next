package com.freightos.pms.adapter.in.web.pms.dto;

import java.util.List;

/**
 * POST /api/pms/performance/search 페이지 응답 DTO.
 */
public record PmsPerformancePageResponse(
    List<PmsPerformanceRowResponse> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {}
