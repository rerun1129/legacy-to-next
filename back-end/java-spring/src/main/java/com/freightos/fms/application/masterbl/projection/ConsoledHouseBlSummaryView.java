package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * application 경계에서 domain projection(ConsoledHouseBlSummary)을 대체하는 view record.
 * SEA/AIR 공통 필드를 포함하며, domain import 없이 application 계층에서 사용된다.
 */
public record ConsoledHouseBlSummaryView(
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
        // SEA 전용
        String etd,
        String eta,
        String vesselName,
        String voyageNo,
        String polCode,
        String podCode,
        // AIR 전용
        BigDecimal chargeWeightKg
) {}
