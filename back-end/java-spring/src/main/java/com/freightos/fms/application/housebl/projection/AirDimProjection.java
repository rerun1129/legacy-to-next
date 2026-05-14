package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;

/**
 * AIR Dim(house_bl_dim) application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record AirDimProjection(
        Long id,
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm,
        Integer quantity,
        BigDecimal cbm,
        BigDecimal volumeWeightKg
) {}
