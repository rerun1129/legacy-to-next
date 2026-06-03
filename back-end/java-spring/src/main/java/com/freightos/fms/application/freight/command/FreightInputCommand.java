package com.freightos.fms.application.freight.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * 운임 헤더 + 라인 전체 저장 커맨드.
 * blType/blId는 포트 메서드 파라미터로 전달되므로 커맨드에 미포함.
 */
public record FreightInputCommand(
    // ── 당사자 3종 ─────────────────────────────────────────────────────────────
    String actualCustomerCode,
    String linerCode,
    String settlePartnerCode,

    // ── 환율 매출 계열 ─────────────────────────────────────────────────────────
    String sellRateDt,
    String sellRateCurrencyCode,
    BigDecimal sellRate,

    // ── 환율 매입 계열 ─────────────────────────────────────────────────────────
    String buyRateDt,
    String buyRateCurrencyCode,
    BigDecimal buyRate,

    // ── 환율 USD 계열 (currencyCode 없음) ─────────────────────────────────────
    String usdRateDt,
    BigDecimal usdRate,

    List<FreightLineCommand> lines
) {}
