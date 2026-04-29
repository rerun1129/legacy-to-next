package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.BlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** House B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record HouseBlDetailResponse(
        Long id,
        String hblNo,
        JobDiv jobDiv,
        Bound bound,
        ShipmentType shipmentType,
        BlType blType,
        FreightTerm freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String docPartnerCode,
        String polCode,
        String podCode,
        String deliveryCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String actualCustomerCode,
        String operatorCode,
        String teamCode,
        String salesManCode,
        Long masterBlId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static HouseBlDetailResponse from(HouseBl entity) {
        return new HouseBlDetailResponse(
                entity.getId(),
                entity.getHblNo() != null ? entity.getHblNo().value() : null,
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipmentType(),
                entity.getBlType(),
                entity.getFreightTerm(),
                entity.getShipperCode() != null ? entity.getShipperCode().value() : null,
                entity.getConsigneeCode() != null ? entity.getConsigneeCode().value() : null,
                entity.getNotifyCode() != null ? entity.getNotifyCode().value() : null,
                entity.getDocPartnerCode() != null ? entity.getDocPartnerCode().value() : null,
                entity.getPolCode() != null ? entity.getPolCode().value() : null,
                entity.getPodCode() != null ? entity.getPodCode().value() : null,
                entity.getDeliveryCode() != null ? entity.getDeliveryCode().value() : null,
                entity.getEtd() != null ? entity.getEtd().asString() : null,
                entity.getEta() != null ? entity.getEta().asString() : null,
                entity.getPkgQty() != null ? entity.getPkgQty().count() : null,
                entity.getPkgUnit(),
                entity.getGrossWeightKg() != null ? entity.getGrossWeightKg().kg() : null,
                entity.getCbm() != null ? entity.getCbm().cbm() : null,
                entity.getActualCustomerCode() != null ? entity.getActualCustomerCode().value() : null,
                entity.getOperatorCode() != null ? entity.getOperatorCode().value() : null,
                entity.getTeamCode() != null ? entity.getTeamCode().value() : null,
                entity.getSalesManCode() != null ? entity.getSalesManCode().value() : null,
                entity.getMasterBlId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
