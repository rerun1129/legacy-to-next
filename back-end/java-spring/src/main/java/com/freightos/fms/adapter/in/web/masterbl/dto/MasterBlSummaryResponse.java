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
                entity.getMblNo() != null ? entity.getMblNo().value() : null,
                entity.getMasterRefNo() != null ? entity.getMasterRefNo().value() : null,
                entity.getJobDiv() != null ? entity.getJobDiv().name() : null,
                entity.getBound(),
                entity.getShipperCode() != null ? entity.getShipperCode().value() : null,
                entity.getConsigneeCode() != null ? entity.getConsigneeCode().value() : null,
                entity.getPolCode() != null ? entity.getPolCode().value() : null,
                entity.getPodCode() != null ? entity.getPodCode().value() : null,
                entity.getEtd() != null ? entity.getEtd().asString() : null,
                entity.getEta() != null ? entity.getEta().asString() : null,
                entity.getOperatorCode() != null ? entity.getOperatorCode().value() : null,
                entity.getCreatedAt()
        );
    }
}
