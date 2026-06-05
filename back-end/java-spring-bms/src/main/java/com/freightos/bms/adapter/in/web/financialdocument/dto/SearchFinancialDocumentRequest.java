package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 금융 서류 전역 검색 요청 DTO.
 * documentTypes는 필수(최소 1개). 나머지는 null/blank 허용(필터 무시).
 * groupFinancialNo: 그룹 번호 like 검색. grouped: "Y"=그룹 있음, "N"=그룹 없음, null/blank=무시.
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
        Integer size,
        String groupFinancialNo,
        String grouped
) {
    /**
     * 하위호환 편의 생성자. 기존 18개 파라미터 호출부를 보존한다.
     * groupFinancialNo·grouped는 null로 초기화된다.
     */
    public SearchFinancialDocumentRequest(
            List<String> documentTypes,
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
    ) {
        this(documentTypes, documentStatus, customerCode, documentNoLike,
             teamCode, operator,
             documentDtFrom, documentDtTo, performanceDtFrom, performanceDtTo,
             etdFrom, etdTo, etaFrom, etaTo,
             jobDiv, bound,
             page, size,
             null, null);
    }
}
