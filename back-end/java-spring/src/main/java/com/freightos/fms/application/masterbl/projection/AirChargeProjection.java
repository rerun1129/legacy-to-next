package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * Master B/L AIR Charge 행의 application-layer projection.
 * enum(Per, FreightTerm, RateClass)과 VO(CurrencyCode, Weight)를 String/BigDecimal로 평탄화하여
 * Adapter(in) 계층의 도메인 타입 의존을 차단한다. (기존 AirDetailProjection 패턴과 동일)
 */
public record AirChargeProjection(
        String freightCode,
        String currencyCode,
        String per,
        String freightTerm,
        BigDecimal grossWeightKg,
        String rateClass,
        BigDecimal chargeWeightKg,
        BigDecimal rate
) {}
