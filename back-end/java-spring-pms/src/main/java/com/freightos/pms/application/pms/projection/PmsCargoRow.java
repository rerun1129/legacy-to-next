package com.freightos.pms.application.pms.projection;

import java.math.BigDecimal;

/**
 * cargo lookup 쿼리 결과 한 행. house_bl_id 기준 keyed 조회.
 */
public record PmsCargoRow(
    Long houseBlId,
    Integer pkgQty,
    BigDecimal cbm,
    BigDecimal grossWeightKg,
    // SEA 전용
    String seaLoadType,
    // AIR 전용
    BigDecimal airChargeWeightKg,
    // TRUCK 전용
    BigDecimal truckChargeWeightKg,
    String truckLoadType,
    // NON_BL 전용
    BigDecimal nonBlRton
) {}
