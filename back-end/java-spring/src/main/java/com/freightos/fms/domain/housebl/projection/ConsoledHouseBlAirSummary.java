package com.freightos.fms.domain.housebl.projection;

import java.math.BigDecimal;

public record ConsoledHouseBlAirSummary(
        Long houseBlId,
        String hblNo,
        String shipperCode,
        String consigneeCode,
        String docPartnerCode,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        BigDecimal chargeWeightKg
) implements ConsoledHouseBlSummary {
}
