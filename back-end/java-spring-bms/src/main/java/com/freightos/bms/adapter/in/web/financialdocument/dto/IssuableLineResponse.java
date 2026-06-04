package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.math.BigDecimal;

/**
 * 발행 가능 운임 라인 조회 응답 DTO.
 * financialDocumentId·documentNo null이면 미발행 상태.
 */
public record IssuableLineResponse(
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
