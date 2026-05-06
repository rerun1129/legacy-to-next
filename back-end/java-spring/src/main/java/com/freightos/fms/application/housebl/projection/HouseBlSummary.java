package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * N+1 문제 해소를 위한 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record HouseBlSummary(
    Long houseBlId,
    String hblNo,
    String jobDiv,
    String bound,
    String polCode,
    String podCode,
    String etd,
    String eta,
    String shipperCode,
    String consigneeCode,
    Integer pkgQty,
    String pkgUnit,
    LocalDateTime createdAt,
    String notifyCode,
    String settlePartnerCode,
    String actualCustomerCode,
    BigDecimal grossWeightKg,
    BigDecimal cbm,
    String vesselName,
    String voyageNo,
    String linerCode,
    String linerName
) {}
