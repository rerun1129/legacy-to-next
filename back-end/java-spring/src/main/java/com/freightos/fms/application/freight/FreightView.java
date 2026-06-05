package com.freightos.fms.application.freight;

import java.math.BigDecimal;
import java.util.List;

/**
 * 운임 헤더 + 라인 조회 VO — BE-B detail 응답 매핑용.
 * application 레이어에 위치. 도메인 엔티티 직접 노출 없음.
 */
public record FreightView(
    // ── 헤더 식별 ──────────────────────────────────────────────────────────────
    Long freightHeaderId,
    String blType,
    Long blId,

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

    // ── 환율 USD 계열 ──────────────────────────────────────────────────────────
    String usdRateDt,
    BigDecimal usdRate,

    List<FreightLineView> lines
) {}
