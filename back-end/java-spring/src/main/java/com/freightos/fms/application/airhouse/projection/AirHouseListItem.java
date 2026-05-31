package com.freightos.fms.application.airhouse.projection;

import java.math.BigDecimal;

/**
 * Air House B/L 리스트 응답 전용 projection.
 * AirHouseSummary(코드만) + code→name 1개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 AirHouseSummaryResponse DTO로 변환한다.
 */
public record AirHouseListItem(
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
        String teamCode,
        String teamName
) {}
