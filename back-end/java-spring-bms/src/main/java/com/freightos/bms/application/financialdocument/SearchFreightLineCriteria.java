package com.freightos.bms.application.financialdocument;

/**
 * 운임 행 발급 화면 전역 조회 조건.
 * 날짜 범위는 yyyyMMdd VARCHAR(8) 컨벤션.
 * issuedStatus: "Y"=발급 완료, "N"=미발급, null/blank=전체.
 * customerCode: 라인의 customer_code 기준(서류검색의 doc.customer_code와 다름).
 */
public record SearchFreightLineCriteria(
        String customerCode,
        String financialDocType,
        String jobDiv,
        String bound,
        String performanceDtFrom,
        String performanceDtTo,
        String issuedStatus,
        Integer page,
        Integer size
) {}
