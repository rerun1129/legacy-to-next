package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.time.LocalDateTime;

/** Master B/L 리스트 화면용 요약 DTO. */
public record MasterBlSummaryResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        Bound bound,
        String shipperCode,
        String consigneeCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String operatorCode,
        LocalDateTime createdAt
) {
    public static MasterBlSummaryResponse from(MasterBl entity) {
        return new MasterBlSummaryResponse(
                entity.getId(),
                entity.getMblNo(),
                entity.getMasterRefNo(),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipperCode(),
                entity.getConsigneeCode(),
                entity.getPolCode(),
                entity.getPodCode(),
                entity.getEtd(),
                entity.getEta(),
                entity.getOperatorCode(),
                entity.getCreatedAt()
        );
    }
}
