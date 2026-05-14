package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;

/**
 * AIR Charge(house_bl_air_charge) application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record AirChargeProjection(
        Long id,
        String freightCode,
        String currencyCode,
        String per,
        String freightTerm,
        BigDecimal grossWeightKg,
        String rateClass,
        BigDecimal chargeWeightKg,
        BigDecimal rate
) {}
