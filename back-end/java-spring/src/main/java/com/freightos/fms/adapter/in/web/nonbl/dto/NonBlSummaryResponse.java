package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.application.nonbl.projection.NonBlSummary;

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
        String        linerName
) {
    /** QueryDSL projection 결과로부터 응답 DTO 생성. NonBlSummary 필드는 이미 String 타입. */
    public static NonBlSummaryResponse from(NonBlSummary summary) {
        return new NonBlSummaryResponse(
                summary.id(),
                summary.hblNo(),
                summary.jobDiv(),
                summary.bound(),
                summary.polCode(),
                summary.podCode(),
                summary.etd(),
                summary.eta(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.createdAt(),
                summary.notifyCode(),
                summary.settlePartnerCode(),
                summary.actualCustomerCode(),
                summary.grossWeightKg(),
                summary.cbm(),
                summary.vesselName(),
                summary.voyageNo(),
                summary.linerCode(),
                summary.linerName()
        );
    }
}
