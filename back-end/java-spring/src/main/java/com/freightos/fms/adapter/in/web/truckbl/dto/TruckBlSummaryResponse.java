package com.freightos.fms.adapter.in.web.truckbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Truck B/L 리스트 화면용 요약 DTO */
public record TruckBlSummaryResponse(
        Long          id,
        String        hblNo,
        JobDiv        jobDiv,
        Bound         bound,
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
        LocalDateTime createdAt
) {
    /** QueryDSL projection 결과로부터 응답 DTO 생성. TruckBlSummary 필드는 이미 raw 타입. */
    public static TruckBlSummaryResponse from(TruckBlSummary summary) {
        return new TruckBlSummaryResponse(
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
                summary.notifyCode(),
                summary.docPartnerCode(),
                summary.truckerCode(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.grossWeightKg(),
                summary.cbm(),
                summary.createdAt()
        );
    }
}
