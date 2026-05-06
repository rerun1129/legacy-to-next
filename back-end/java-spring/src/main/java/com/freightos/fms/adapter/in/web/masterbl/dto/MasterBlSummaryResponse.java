package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;

import java.time.LocalDateTime;

/** Master B/L 리스트 화면용 요약 DTO. */
public record MasterBlSummaryResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        String bound,
        String shipperCode,
        String consigneeCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String operatorCode,
        LocalDateTime createdAt
) {
    public static MasterBlSummaryResponse from(MasterBlSummaryResult result) {
        return new MasterBlSummaryResponse(
                result.id(),
                result.mblNo(),
                result.masterRefNo(),
                result.jobDiv(),
                result.bound(),
                result.shipperCode(),
                result.consigneeCode(),
                result.polCode(),
                result.podCode(),
                result.etd(),
                result.eta(),
                result.operatorCode(),
                result.createdAt()
        );
    }
}
