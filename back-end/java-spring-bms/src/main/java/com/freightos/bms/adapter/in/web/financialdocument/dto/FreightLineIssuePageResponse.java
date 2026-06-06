package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.util.List;

/**
 * 운임 행 발급 화면 페이지 응답 DTO.
 */
public record FreightLineIssuePageResponse(
        List<FreightLineIssueRowResponse> content,
        long totalElements,
        int totalPages,
        int pageNumber,
        int pageSize
) {}
