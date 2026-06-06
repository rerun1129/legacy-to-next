package com.freightos.bms.adapter.in.web.financialdocument.dto;

/**
 * 운임 행 발급 화면 전역 조회 요청 DTO.
 * issuedStatus: "Y"=발급 완료, "N"=미발급, null=전체.
 * 날짜는 yyyyMMdd 형식.
 */
public record SearchFreightLineRequest(
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
