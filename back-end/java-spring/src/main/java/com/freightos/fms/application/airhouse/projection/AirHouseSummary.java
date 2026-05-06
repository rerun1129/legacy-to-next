package com.freightos.fms.application.airhouse.projection;

import java.math.BigDecimal;

/**
 * Air House B/L 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record AirHouseSummary(
    Long id,
    String hblNo,
    String bound,
    String mblNo,
    String shipmentType,
    String etd,
    String eta,
    BigDecimal grossWeightKg,
    BigDecimal chargeWeightKg,
    Integer pkgQty,
    String pkgUnit,
    String polCode,
    String podCode,
    String shipperCode,
    String consigneeCode,
    String notifyCode,
    String settlePartnerCode,
    String docPartnerCode,
    String airlineCode,
    String masterRefNo,
    String freightTerm,
    String incoterms,
    String actualCustomerCode,
    String salesManCode,
    String teamCode
) {}
