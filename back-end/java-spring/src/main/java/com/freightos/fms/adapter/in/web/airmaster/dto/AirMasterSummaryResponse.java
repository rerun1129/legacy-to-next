package com.freightos.fms.adapter.in.web.airmaster.dto;

import com.freightos.fms.application.airmaster.projection.AirMasterListItem;

import java.math.BigDecimal;

/** Air Master B/L 리스트 화면용 요약 DTO */
public record AirMasterSummaryResponse(
        Long       id,
        String     bound,
        String     mblNo,
        String     shipmentType,
        String     etd,
        String     eta,
        BigDecimal grossWeightKg,
        BigDecimal chargeWeightKg,
        Integer    pkgQty,
        String     pkgUnit,
        Long       houseBlCount,
        String     polCode,
        String     podCode,
        String     shipperCode,
        String     consigneeCode,
        String     notifyCode,
        String     settlePartnerCode,
        String     airlineCode,
        String     masterRefNo,
        String     freightTerm,
        String     operatorCode,
        String     teamCode,
        String     teamName
) {
    public static AirMasterSummaryResponse from(AirMasterListItem item) {
        return new AirMasterSummaryResponse(
                item.id(),
                item.bound(),
                item.mblNo(),
                item.shipmentType(),
                item.etd(),
                item.eta(),
                item.grossWeightKg(),
                item.chargeWeightKg(),
                item.pkgQty(),
                item.pkgUnit(),
                item.houseBlCount(),
                item.polCode(),
                item.podCode(),
                item.shipperCode(),
                item.consigneeCode(),
                item.notifyCode(),
                item.settlePartnerCode(),
                item.airlineCode(),
                item.masterRefNo(),
                item.freightTerm(),
                item.operatorCode(),
                item.teamCode(),
                item.teamName()
        );
    }
}
