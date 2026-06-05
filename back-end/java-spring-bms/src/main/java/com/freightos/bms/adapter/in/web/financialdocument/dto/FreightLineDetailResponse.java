package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.math.BigDecimal;

/**
 * 운임 라인 디테일 응답 DTO. B-02 전 컬럼 포함.
 */
public record FreightLineDetailResponse(
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
