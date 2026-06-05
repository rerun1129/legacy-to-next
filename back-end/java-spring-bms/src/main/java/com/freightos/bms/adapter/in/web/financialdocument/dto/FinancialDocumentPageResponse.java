package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.util.List;

/**
 * 금융 서류 전역 검색 페이지 응답 래퍼 DTO.
 */
public record FinancialDocumentPageResponse(
        List<FinancialDocumentSearchResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}
