package com.freightos.fms.application.seahouse.projection;

import java.math.BigDecimal;

/**
 * Sea House B/L 리스트 응답 전용 projection.
 * SeaHouseSummary(코드만) + code→name 7개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 SeaHouseSummaryResponse DTO로 변환한다.
 */
public record SeaHouseListItem(
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
        String shipperName,
        String consigneeCode,
        String consigneeName,
        String notifyCode,
        String notifyName,
        String settlePartnerCode,
        String settlePartnerName,
        String docPartnerCode,
        String docPartnerName,
        String linerCode,
        String linerName,
        String masterRefNo,
        String freightTerm,
        String incoterms,
        String actualCustomerCode,
        String actualCustomerName,
        String salesManCode,
        String teamCode,
        String teamName,
        String loadType,
        BigDecimal cbm,
        String deliveryCode,
        String vesselName,
        String voyageNo,
        Long cntr20Qty,
        Long cntr40Qty,
        Long lengthFeetSum
) {}
