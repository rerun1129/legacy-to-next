package com.freightos.fms.application.airmaster.projection;

import java.math.BigDecimal;

/**
 * Air Master B/L 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record AirMasterSummary(
    Long id,
    String bound,
    String mblNo,
    String shipmentType,
    String etd,
    String eta,
    BigDecimal grossWeightKg,
    BigDecimal chargeWeightKg,
    Integer pkgQty,
    String pkgUnit,
    Long houseBlCount,
    String polCode,
    String podCode,
    String shipperCode,
    String consigneeCode,
    String notifyCode,
    String settlePartnerCode,
    String airlineCode,
    String masterRefNo,
    String freightTerm,
    String operatorCode,
    String teamCode
) {}
