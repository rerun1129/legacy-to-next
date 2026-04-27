package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Master B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record MasterBlDetailResponse(
        UUID id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        Bound bound,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String polCode,
        String podCode,
        LocalDate etd,
        LocalDate eta,
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
                entity.getMblNo(),
                entity.getMasterRefNo(),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipperCode(),
                entity.getConsigneeCode(),
                entity.getNotifyCode(),
                entity.getPolCode(),
                entity.getPodCode(),
                entity.getEtd(),
                entity.getEta(),
                entity.getFreightTerm(),
                entity.getOperatorCode(),
                entity.getTeamCode(),
                entity.getPkgQty(),
                entity.getPkgUnit(),
                entity.getGrossWeightKg(),
                entity.getCbm(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
