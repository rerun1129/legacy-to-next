package com.freightos.fms.adapter.in.web.airhouse.dto;

import com.freightos.fms.application.airhouse.projection.AirHouseListItem;

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
        String     teamCode,
        String     teamName
) {
    public static AirHouseSummaryResponse from(AirHouseListItem item) {
        return new AirHouseSummaryResponse(
                item.id(),
                item.hblNo(),
                item.bound(),
                item.mblNo(),
                item.shipmentType(),
                item.etd(),
                item.eta(),
                item.grossWeightKg(),
                item.chargeWeightKg(),
                item.pkgQty(),
                item.pkgUnit(),
                item.polCode(),
                item.podCode(),
                item.shipperCode(),
                item.consigneeCode(),
                item.notifyCode(),
                item.settlePartnerCode(),
                item.docPartnerCode(),
                item.airlineCode(),
                item.masterRefNo(),
                item.freightTerm(),
                item.incoterms(),
                item.actualCustomerCode(),
                item.salesManCode(),
                item.teamCode(),
                item.teamName()
        );
    }
}
