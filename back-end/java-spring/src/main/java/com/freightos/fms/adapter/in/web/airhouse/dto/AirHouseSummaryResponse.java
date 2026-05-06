package com.freightos.fms.adapter.in.web.airhouse.dto;

import com.freightos.fms.application.airhouse.projection.AirHouseSummary;

import java.math.BigDecimal;

/** Air House B/L 리스트 화면용 요약 DTO */
public record AirHouseSummaryResponse(
        Long       id,
        String     hblNo,
        String     bound,
        String     mblNo,
        String     shipmentType,
        String     etd,
        String     eta,
        BigDecimal grossWeightKg,
        BigDecimal chargeWeightKg,
        Integer    pkgQty,
        String     pkgUnit,
        String     polCode,
        String     podCode,
        String     shipperCode,
        String     consigneeCode,
        String     notifyCode,
        String     settlePartnerCode,
        String     docPartnerCode,
        String     airlineCode,
        String     masterRefNo,
        String     freightTerm,
        String     incoterms,
        String     actualCustomerCode,
        String     salesManCode,
        String     teamCode
) {
    public static AirHouseSummaryResponse from(AirHouseSummary summary) {
        return new AirHouseSummaryResponse(
                summary.id(),
                summary.hblNo(),
                summary.bound(),
                summary.mblNo(),
                summary.shipmentType(),
                summary.etd(),
                summary.eta(),
                summary.grossWeightKg(),
                summary.chargeWeightKg(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.polCode(),
                summary.podCode(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.notifyCode(),
                summary.settlePartnerCode(),
                summary.docPartnerCode(),
                summary.airlineCode(),
                summary.masterRefNo(),
                summary.freightTerm(),
                summary.incoterms(),
                summary.actualCustomerCode(),
                summary.salesManCode(),
                summary.teamCode()
        );
    }
}
