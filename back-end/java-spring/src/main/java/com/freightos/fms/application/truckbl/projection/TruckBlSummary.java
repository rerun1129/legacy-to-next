package com.freightos.fms.application.truckbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Truck B/L 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record TruckBlSummary(
    Long id,
    String hblNo,
    String jobDiv,
    String bound,
    String polCode,
    String podCode,
    String etd,
    String eta,
    String shipperCode,
    String consigneeCode,
    String notifyCode,
    String docPartnerCode,
    String truckerCode,
    Integer pkgQty,
    String pkgUnit,
    BigDecimal grossWeightKg,
    BigDecimal cbm,
    LocalDateTime createdAt
) {}
