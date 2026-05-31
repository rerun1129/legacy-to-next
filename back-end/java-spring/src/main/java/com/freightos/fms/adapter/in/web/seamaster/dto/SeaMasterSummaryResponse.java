package com.freightos.fms.adapter.in.web.seamaster.dto;

import com.freightos.fms.application.seamaster.projection.SeaMasterListItem;

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
        String     teamName,
        String     vesselName,
        String     voyageNo,
        String     loadType,
        BigDecimal cbm
) {
    public static SeaMasterSummaryResponse from(SeaMasterListItem item) {
        return new SeaMasterSummaryResponse(
                item.id(),
                item.bound(),
                item.mblNo(),
                item.shipmentType(),
                item.etd(),
                item.eta(),
                item.grossWeightKg(),
                item.rton(),
                item.pkgQty(),
                item.pkgUnit(),
                item.houseBlCount(),
                item.polCode(),
                item.podCode(),
                item.shipperCode(),
                item.consigneeCode(),
                item.notifyCode(),
                item.settlePartnerCode(),
                item.linerCode(),
                item.masterRefNo(),
                item.freightTerm(),
                item.operatorCode(),
                item.teamCode(),
                item.teamName(),
                item.vesselName(),
                item.voyageNo(),
                item.loadType(),
                item.cbm()
        );
    }
}
