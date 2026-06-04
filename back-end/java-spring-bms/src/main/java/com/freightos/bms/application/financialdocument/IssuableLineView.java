package com.freightos.bms.application.financialdocument;

import java.math.BigDecimal;

/**
 * 발행 가능 운임 라인 조회 뷰 VO. Application → Adapter(in) 방향 전달 객체.
 * financialDocumentId가 null이면 미발행, 값이 있으면 이미 발행된 라인.
 */
public record IssuableLineView(
        Long freightLineId,
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
        String documentNo
) {}
