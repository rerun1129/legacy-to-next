package com.freightos.fms.adapter.in.web.airmaster.dto;

import com.freightos.fms.application.airmaster.projection.AirMasterSummary;

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
        String     teamCode
) {
    public static AirMasterSummaryResponse from(AirMasterSummary summary) {
        return new AirMasterSummaryResponse(
                summary.id(),
                summary.bound(),
                summary.mblNo(),
                summary.shipmentType(),
                summary.etd(),
                summary.eta(),
                summary.grossWeightKg(),
                summary.chargeWeightKg(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.houseBlCount(),
                summary.polCode(),
                summary.podCode(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.notifyCode(),
                summary.settlePartnerCode(),
                summary.airlineCode(),
                summary.masterRefNo(),
                summary.freightTerm(),
                summary.operatorCode(),
                summary.teamCode()
        );
    }
}
