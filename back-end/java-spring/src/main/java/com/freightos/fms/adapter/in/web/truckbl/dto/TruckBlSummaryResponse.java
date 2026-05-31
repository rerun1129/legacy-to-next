package com.freightos.fms.adapter.in.web.truckbl.dto;

import com.freightos.fms.application.truckbl.projection.TruckBlListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Truck B/L 리스트 화면용 요약 DTO */
public record TruckBlSummaryResponse(
        Long          id,
        String        hblNo,
        String        jobDiv,
        String        bound,
        String        polCode,
        String        podCode,
        String        etd,
        String        eta,
        String        shipperCode,
        String        consigneeCode,
        String        notifyCode,
        String        docPartnerCode,
        String        truckerCode,
        Integer       pkgQty,
        String        pkgUnit,
        BigDecimal    grossWeightKg,
        BigDecimal    cbm,
        LocalDateTime createdAt,
        String        teamCode,
        String        teamName
) {
    public static TruckBlSummaryResponse from(TruckBlListItem item) {
        return new TruckBlSummaryResponse(
                item.id(),
                item.hblNo(),
                item.jobDiv(),
                item.bound(),
                item.polCode(),
                item.podCode(),
                item.etd(),
                item.eta(),
                item.shipperCode(),
                item.consigneeCode(),
                item.notifyCode(),
                item.docPartnerCode(),
                item.truckerCode(),
                item.pkgQty(),
                item.pkgUnit(),
                item.grossWeightKg(),
                item.cbm(),
                item.createdAt(),
                item.teamCode(),
                item.teamName()
        );
    }
}
