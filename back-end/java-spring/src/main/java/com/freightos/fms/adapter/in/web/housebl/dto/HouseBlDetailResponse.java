package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.common.enums.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.freightos.common.util.VoMapper.mapOrNull;

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
        WeightUnit pkgUnit,
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
                mapOrNull(entity.getHblNo(), BlNumber::value),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getShipmentType(),
                entity instanceof HouseBlSea sea ? sea.getBlType() : null,
                entity.getFreightTerm(),
                mapOrNull(entity.getShipperCode(), CustomerCode::value),
                mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                mapOrNull(entity.getDocPartnerCode(), CustomerCode::value),
                mapOrNull(entity.getPolCode(), PortCode::value),
                mapOrNull(entity.getPodCode(), PortCode::value),
                mapOrNull(entity.getDeliveryCode(), PortCode::value),
                mapOrNull(entity.getEtd(), BlDate::asString),
                mapOrNull(entity.getEta(), BlDate::asString),
                mapOrNull(entity.getPkgQty(), Quantity::count),
                entity.getPkgUnit(),
                mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                mapOrNull(entity.getCbm(), Volume::cbm),
                mapOrNull(entity.getActualCustomerCode(), CustomerCode::value),
                mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                mapOrNull(entity.getTeamCode(), TeamCode::value),
                mapOrNull(entity.getSalesManCode(), EmployeeCode::value),
                entity.getMasterBlId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
