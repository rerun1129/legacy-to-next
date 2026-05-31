package com.freightos.fms.application.seamaster.projection;

import java.math.BigDecimal;

/**
 * Sea Master B/L 리스트 응답 전용 projection.
 * SeaMasterSummary(코드만) + code→name 1개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 SeaMasterSummaryResponse DTO로 변환한다.
 */
public record SeaMasterListItem(
        Long id,
        String bound,
        String mblNo,
        String shipmentType,
        String etd,
        String eta,
        BigDecimal grossWeightKg,
        BigDecimal rton,
        Integer pkgQty,
        String pkgUnit,
        Long houseBlCount,
        String polCode,
        String podCode,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String settlePartnerCode,
        String linerCode,
        String masterRefNo,
        String freightTerm,
        String operatorCode,
        String teamCode,
        String teamName,
        String vesselName,
        String voyageNo,
        String loadType,
        BigDecimal cbm
) {}
