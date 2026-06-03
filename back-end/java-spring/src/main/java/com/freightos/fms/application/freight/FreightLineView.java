package com.freightos.fms.application.freight;

import java.math.BigDecimal;

/**
 * 운임 라인 조회 VO — 입력값 + 계산값 포함.
 */
public record FreightLineView(
    Long freightLineId,

    // ── 입력값 ──────────────────────────────────────────────────────────────────
    String freightType,
    String freightCode,
    String per,
    BigDecimal unitQuantity,
    BigDecimal unitPrice,
    String currency,
    String customerCode,
    String taxType,
    String performanceDt,

    // ── 계산값 ──────────────────────────────────────────────────────────────────
    String financialDocType,
    BigDecimal exchangeRate,
    BigDecimal settleAmount,
    BigDecimal localAmount,
    BigDecimal settleTaxAmount,
    BigDecimal localTaxAmount,
    BigDecimal usdExchangeRate,
    BigDecimal usdAmount,

    // ── 단계B 진입 시 채워질 필드 ─────────────────────────────────────────────
    String taxNo,
    String taxDt,
    String slipNo,
    String slipDt,
    Long financialDocumentId
) {}
