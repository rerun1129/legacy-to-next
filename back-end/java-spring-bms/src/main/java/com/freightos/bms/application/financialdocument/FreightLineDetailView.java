package com.freightos.bms.application.financialdocument;

import java.math.BigDecimal;

/**
 * 운임 라인 디테일 뷰 VO. Application → Adapter(in) 방향 전달 객체.
 * B-02 전 컬럼 포함. freightName·customerName은 QueryService에서 resolve된다.
 */
public record FreightLineDetailView(
        Long freightLineId,
        Long freightHeaderId,
        String freightType,
        String financialDocType,
        String freightCode,
        String freightName,
        BigDecimal unitQuantity,
        BigDecimal unitPrice,
        String per,
        String currency,
        BigDecimal exchangeRate,
        BigDecimal settleAmount,
        BigDecimal localAmount,
        BigDecimal settleTaxAmount,
        BigDecimal localTaxAmount,
        BigDecimal usdExchangeRate,
        BigDecimal usdAmount,
        String customerCode,
        String customerName,
        String taxType,
        String taxNo,
        String taxDt,
        String slipNo,
        String slipDt,
        String performanceDt,
        Long financialDocumentId
) {}
