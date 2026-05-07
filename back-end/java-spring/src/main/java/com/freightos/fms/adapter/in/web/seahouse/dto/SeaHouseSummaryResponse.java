package com.freightos.fms.adapter.in.web.seahouse.dto;

import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;

import java.math.BigDecimal;

/** Sea House B/L 리스트 화면용 요약 DTO */
public record SeaHouseSummaryResponse(
        Long       id,
        String     hblNo,
        String     bound,
        String     mblNo,
        String     shipmentType,
        String     etd,
        String     eta,
        BigDecimal grossWeightKg,
        BigDecimal rton,
        Integer    pkgQty,
        String     pkgUnit,
        String     polCode,
        String     podCode,
        String     shipperCode,
        String     consigneeCode,
        String     notifyCode,
        String     settlePartnerCode,
        String     docPartnerCode,
        String     linerCode,
        String     masterRefNo,
        String     freightTerm,
        String     incoterms,
        String     actualCustomerCode,
        String     salesManCode,
        String     teamCode,
        String     loadType,
        BigDecimal cbm,
        String     deliveryCode,
        String     vesselName,
        String     voyageNo,
        Long       cntr20Qty,
        Long       cntr40Qty,
        BigDecimal teuQty
) {
    public static SeaHouseSummaryResponse from(SeaHouseSummary summary) {
        BigDecimal teuQty = summary.lengthFeetSum() != null
            ? BigDecimal.valueOf(summary.lengthFeetSum()).divide(BigDecimal.valueOf(20))
            : null;
        return new SeaHouseSummaryResponse(
                summary.id(),
                summary.hblNo(),
                summary.bound(),
                summary.mblNo(),
                summary.shipmentType(),
                summary.etd(),
                summary.eta(),
                summary.grossWeightKg(),
                summary.rton(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.polCode(),
                summary.podCode(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.notifyCode(),
                summary.settlePartnerCode(),
                summary.docPartnerCode(),
                summary.linerCode(),
                summary.masterRefNo(),
                summary.freightTerm(),
                summary.incoterms(),
                summary.actualCustomerCode(),
                summary.salesManCode(),
                summary.teamCode(),
                summary.loadType(),
                summary.cbm(),
                summary.deliveryCode(),
                summary.vesselName(),
                summary.voyageNo(),
                summary.cntr20Qty(),
                summary.cntr40Qty(),
                teuQty
        );
    }
}
