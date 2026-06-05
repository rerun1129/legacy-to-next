package com.freightos.bms.application.financialdocument;

import java.math.BigDecimal;

/**
 * 금융 서류 조회 뷰 VO. Application → Adapter(in) 방향 전달 객체.
 */
public record FinancialDocumentView(
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
        String operator,
        String groupFinancialNo
) {}
