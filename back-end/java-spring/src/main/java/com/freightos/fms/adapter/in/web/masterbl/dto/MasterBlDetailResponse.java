package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Master B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record MasterBlDetailResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        Bound bound,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        FreightTerm freightTerm,
        String operatorCode,
        String teamCode,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MasterBlDetailResponse from(MasterBl entity) {
        return new MasterBlDetailResponse(
                entity.getId(),
                entity.getMblNo() != null ? entity.getMblNo().value() : null,
                entity.getMasterRefNo() != null ? entity.getMasterRefNo().value() : null,
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipperCode() != null ? entity.getShipperCode().value() : null,
                entity.getConsigneeCode() != null ? entity.getConsigneeCode().value() : null,
                entity.getNotifyCode() != null ? entity.getNotifyCode().value() : null,
                entity.getPolCode() != null ? entity.getPolCode().value() : null,
                entity.getPodCode() != null ? entity.getPodCode().value() : null,
                entity.getEtd() != null ? entity.getEtd().asString() : null,
                entity.getEta() != null ? entity.getEta().asString() : null,
                entity.getFreightTerm(),
                entity.getOperatorCode() != null ? entity.getOperatorCode().value() : null,
                entity.getTeamCode() != null ? entity.getTeamCode().value() : null,
                entity.getPkgQty() != null ? entity.getPkgQty().count() : null,
                entity.getPkgUnit(),
                entity.getGrossWeightKg() != null ? entity.getGrossWeightKg().kg() : null,
                entity.getCbm() != null ? entity.getCbm().cbm() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
