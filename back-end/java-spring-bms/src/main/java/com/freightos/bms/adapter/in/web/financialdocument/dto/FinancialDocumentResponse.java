package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.math.BigDecimal;

/**
 * 금융 서류 조회 응답 DTO. documentType·status는 String으로 노출.
 */
public record FinancialDocumentResponse(
        Long financialDocumentId,
        String documentNo,
        String documentType,
        String documentDt,
        String status,
        String customerCode,
        String customerName,
        BigDecimal settleTotalAmount,
        BigDecimal localTotalAmount,
        BigDecimal settleTotalVat,
        BigDecimal localTotalVat,
        BigDecimal usdTotalAmount,
        String performanceDt,
        String teamCode,
        String operator
) {}
