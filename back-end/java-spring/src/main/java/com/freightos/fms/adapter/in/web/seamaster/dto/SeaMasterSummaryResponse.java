package com.freightos.fms.adapter.in.web.seamaster.dto;

import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;

import java.math.BigDecimal;

/** Sea Master B/L 리스트 화면용 요약 DTO */
public record SeaMasterSummaryResponse(
        Long       id,
        String     bound,
        String     mblNo,
        String     shipmentType,
        String     etd,
        String     eta,
        BigDecimal grossWeightKg,
        BigDecimal rton,
        Integer    pkgQty,
        String     pkgUnit,
        Long       houseBlCount,
        String     polCode,
        String     podCode,
        String     shipperCode,
        String     consigneeCode,
        String     notifyCode,
        String     settlePartnerCode,
        String     linerCode,
        String     masterRefNo,
        String     freightTerm,
        String     operatorCode,
        String     teamCode,
        String     vesselName,
        String     voyageNo,
        String     loadType,
        BigDecimal cbm
) {
    public static SeaMasterSummaryResponse from(SeaMasterSummary summary) {
        return new SeaMasterSummaryResponse(
                summary.id(),
                summary.bound(),
                summary.mblNo(),
                summary.shipmentType(),
                summary.etd(),
                summary.eta(),
                summary.grossWeightKg(),
                summary.rton(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.houseBlCount(),
                summary.polCode(),
                summary.podCode(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.notifyCode(),
                summary.settlePartnerCode(),
                summary.linerCode(),
                summary.masterRefNo(),
                summary.freightTerm(),
                summary.operatorCode(),
                summary.teamCode(),
                summary.vesselName(),
                summary.voyageNo(),
                summary.loadType(),
                summary.cbm()
        );
    }
}
