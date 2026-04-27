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
                entity.getHblNo(),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipmentType(),
                entity.getBlType(),
                entity.getFreightTerm(),
                entity.getShipperCode(),
                entity.getConsigneeCode(),
                entity.getNotifyCode(),
                entity.getDocPartnerCode(),
                entity.getPolCode(),
                entity.getPodCode(),
                entity.getDeliveryCode(),
                entity.getEtd(),
                entity.getEta(),
                entity.getPkgQty(),
                entity.getPkgUnit(),
                entity.getGrossWeightKg(),
                entity.getCbm(),
                entity.getActualCustomerCode(),
                entity.getOperatorCode(),
                entity.getTeamCode(),
                entity.getSalesManCode(),
                entity.getMasterBlId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
