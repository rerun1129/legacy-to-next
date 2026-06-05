package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 금융 서류 발행 요청 DTO.
 */
public record IssueDocumentRequest(
        @NotBlank String blType,
        @NotNull Long blId,
        @NotBlank String freightType,
        @NotEmpty List<Long> lineIds,
        @NotBlank String documentDt,
        @NotBlank String performanceDt,
        String teamCode,
        String operator
) {}
