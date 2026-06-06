package com.freightos.bms.application.financialdocument;

import java.math.BigDecimal;

/**
 * 운임 행 발급 화면 그리드 행 뷰.
 * 발급 번호/발급일은 null 허용(미발급 행). customerName/blNo는 Service에서 resolve.
 */
public record FreightLineIssueRowView(
        Long freightLineId,
        Long freightHeaderId,
        String blType,
        Long blId,
        String blNo,
        String jobDiv,
        String bound,
        String etd,
        String freightType,
        String financialDocType,
        String freightCode,
        String customerCode,
        String customerName,
        String currency,
        BigDecimal settleAmount,
        BigDecimal localAmount,
        BigDecimal settleTaxAmount,
        BigDecimal localTaxAmount,
        BigDecimal usdAmount,
        String performanceDt,
        Long financialDocumentId,
        String documentNo,
        String documentStatus,
        String taxNo,
        String taxDt,
        String slipNo,
        String slipDt
) {}
