package com.freightos.fms.application.airmaster.projection;

import java.math.BigDecimal;

/**
 * Air Master B/L 리스트 응답 전용 projection.
 * AirMasterSummary(코드만) + code→name 1개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 AirMasterSummaryResponse DTO로 변환한다.
 */
public record AirMasterListItem(
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
        String teamCode,
        String teamName
) {}
