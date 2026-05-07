package com.freightos.fms.application.seahouse.projection;

import java.math.BigDecimal;

/**
 * Sea House B/L 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 enum 필드를 String으로 통일한다.
 * lengthFeetSum: 컨테이너 length_feet 합계(Long). DB sum()은 런타임에 Long 반환. Response에서 /20 → BigDecimal teuQty 변환.
 */
public record SeaHouseSummary(
    Long id,
    String hblNo,
    String bound,
    String mblNo,
    String shipmentType,
    String etd,
    String eta,
    BigDecimal grossWeightKg,
    BigDecimal rton,
    Integer pkgQty,
    String pkgUnit,
    String polCode,
    String podCode,
    String shipperCode,
    String consigneeCode,
    String notifyCode,
    String settlePartnerCode,
    String docPartnerCode,
    String linerCode,
    String masterRefNo,
    String freightTerm,
    String incoterms,
    String actualCustomerCode,
    String salesManCode,
    String teamCode,
    String loadType,
    BigDecimal cbm,
    String deliveryCode,
    String vesselName,
    String voyageNo,
    Long cntr20Qty,
    Long cntr40Qty,
    Long lengthFeetSum
) {}
