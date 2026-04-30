package com.freightos.fms.domain.housebl.projection;

import java.math.BigDecimal;

public record ConsoledHouseBlSeaSummary(
        Long houseBlId,
        String hblNo,
        String shipperCode,
        String consigneeCode,
        String docPartnerCode,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String etd,
        String eta,
        String vesselName,
        String voyageNo,
        String polCode,
        String podCode
) implements ConsoledHouseBlSummary {
}
