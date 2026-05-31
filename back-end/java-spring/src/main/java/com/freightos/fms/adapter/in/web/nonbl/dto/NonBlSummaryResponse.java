package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.application.nonbl.projection.NonBlListItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Non B/L 리스트 화면용 요약 DTO */
public record NonBlSummaryResponse(
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
        Integer       pkgQty,
        String        pkgUnit,
        LocalDateTime createdAt,
        String        notifyCode,
        String        settlePartnerCode,
        String        actualCustomerCode,
        BigDecimal    grossWeightKg,
        BigDecimal    cbm,
        String        vesselName,
        String        voyageNo,
        String        linerCode,
        String        linerName,
        String        teamCode,
        String        teamName
) {
    /** NonBlListItem으로부터 응답 DTO 생성. */
    public static NonBlSummaryResponse from(NonBlListItem item) {
        return new NonBlSummaryResponse(
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
                item.pkgQty(),
                item.pkgUnit(),
                item.createdAt(),
                item.notifyCode(),
                item.settlePartnerCode(),
                item.actualCustomerCode(),
                item.grossWeightKg(),
                item.cbm(),
                item.vesselName(),
                item.voyageNo(),
                item.linerCode(),
                item.linerName(),
                item.teamCode(),
                item.teamName()
        );
    }
}
