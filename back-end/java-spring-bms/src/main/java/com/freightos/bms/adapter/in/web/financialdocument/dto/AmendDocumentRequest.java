package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 금융 서류 편집(amend) 요청 DTO.
 * finalLineIds에 @NotEmpty/@NotNull 금지 — 빈 리스트는 서류 전체 삭제를 의미한다.
 * 헤더 4필드(documentDt·performanceDt·teamCode·operator)는 검증 어노테이션 없음 — 편집하지 않을 때 null 허용.
 */
public record AmendDocumentRequest(
        @NotBlank String blType,
        @NotBlank String blId,
        @NotBlank String freightType,
        List<Long> finalLineIds,
        String documentDt,
        String performanceDt,
        String teamCode,
        String operator
) {}
