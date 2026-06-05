package com.freightos.bms.application.financialdocument;

import java.util.List;

/**
 * 금융 서류 전역 검색 조건. Controller → Application 방향 전달 객체.
 * documentTypes는 필수(비어있으면 빈 결과). 나머지는 null/blank 시 필터 무시.
 * 날짜 범위는 yyyyMMdd VARCHAR(8) 컨벤션.
 * groupFinancialNo: 그룹 번호 like 검색. grouped: "Y"=그룹 있음, "N"=그룹 없음, null/blank=무시.
 */
public record SearchFinancialDocumentCriteria(
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
        String groupFinancialNo,
        String grouped
) {
    /**
     * 하위호환 편의 생성자. 기존 16개 파라미터 호출부를 보존한다.
     * groupFinancialNo·grouped는 null로 초기화된다.
     */
    public SearchFinancialDocumentCriteria(
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
            String bound
    ) {
        this(documentTypes, documentStatus, customerCode, documentNoLike,
             teamCode, operator,
             documentDtFrom, documentDtTo, performanceDtFrom, performanceDtTo,
             etdFrom, etdTo, etaFrom, etaTo,
             jobDiv, bound,
             null, null);
    }
}
