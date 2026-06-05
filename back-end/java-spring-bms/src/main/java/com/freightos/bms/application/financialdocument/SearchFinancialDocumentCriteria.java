package com.freightos.bms.application.financialdocument;

import java.util.List;

/**
 * 금융 서류 전역 검색 조건. Controller → Application 방향 전달 객체.
 * documentTypes는 필수(비어있으면 빈 결과). 나머지는 null/blank 시 필터 무시.
 * 날짜 범위는 yyyyMMdd VARCHAR(8) 컨벤션.
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
        String bound
) {}
