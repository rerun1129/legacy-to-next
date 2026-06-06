package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.math.BigDecimal;

/**
 * 운임 행 발급 화면 그리드 행 응답 DTO.
 * FE zod 1:1 매핑 대상. taxNo·slipNo·taxDt·slipDt는 nullable.
 */
public record FreightLineIssueRowResponse(
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
