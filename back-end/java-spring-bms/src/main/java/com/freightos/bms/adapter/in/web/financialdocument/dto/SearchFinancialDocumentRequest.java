package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 금융 서류 전역 검색 요청 DTO.
 * documentTypes는 필수(최소 1개). 나머지는 null/blank 허용(필터 무시).
 */
public record SearchFinancialDocumentRequest(
        @NotEmpty List<String> documentTypes,
        String documentStatus,
        String customerCode,
        String documentNoLike,
        String teamCode,
        String operator,
        String documentDtFrom,
        String documentDtTo,
        String performanceDtFrom,
        String performanceDtTo,
        String etdFrom,
        String etdTo,
        String etaFrom,
        String etaTo,
        String jobDiv,
        String bound,
        Integer page,
        Integer size
) {}
