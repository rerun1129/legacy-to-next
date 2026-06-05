package com.freightos.bms.application.financialdocument;

import java.math.BigDecimal;

/**
 * 금융 서류 전역 검색 결과 뷰 VO. Application → Adapter(in) 방향 전달 객체.
 * customerName·teamName·operatorName·B/L 파생 필드(jobDiv·bound·blNo·etd·eta)는
 * QueryService에서 CodeNameResolver를 통해 resolve된다.
 */
public record FinancialDocumentSearchView(
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
        // B/L 파생 (FMS cross-schema)
        String blType,
        Long blId,
        String jobDiv,
        String bound,
        String blNo,
        String etd,
        String eta
) {}
