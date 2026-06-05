package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.math.BigDecimal;

/**
 * 금융 서류 전역 검색 결과 단건 응답 DTO.
 */
public record FinancialDocumentSearchResponse(
        Long financialDocumentId,
        String documentNo,
        String documentType,
        String documentDt,
        String documentStatus,
        String customerCode,
        String customerName,
        BigDecimal settleTotalAmount,
        BigDecimal localTotalAmount,
        BigDecimal settleTotalVat,
        BigDecimal localTotalVat,
        BigDecimal usdTotalAmount,
        String performanceDt,
        String teamCode,
        String teamName,
        String operator,
        String operatorName,
        String groupFinancialNo,
        String blType,
        Long blId,
        String jobDiv,
        String bound,
        String blNo,
        String etd,
        String eta
) {}
