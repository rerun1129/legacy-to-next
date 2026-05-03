package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
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
        LocalDateTime updatedAt,

        // Non B/L 전용 필드
        String originalBlRef,
        String workDivision,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton
) {
    public static HouseBlDetailResponse from(HouseBl entity) {
        HouseBlNonBl nonBl = entity instanceof HouseBlNonBl n ? n : null;

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
                entity.getUpdatedAt(),

                // Non B/L 전용 필드
                nonBl != null ? mapOrNull(nonBl.getOriginalBlRef(), BlNumber::value) : null,
                nonBl != null && nonBl.getWorkDivision() != null ? nonBl.getWorkDivision().name() : null,
                nonBl != null ? nonBl.getLinerCode() : null,
                nonBl != null ? nonBl.getLinerName() : null,
                nonBl != null ? nonBl.getVesselName() : null,
                nonBl != null ? nonBl.getVoyageNo() : null,
                nonBl != null ? nonBl.getFinalDestCode() : null,
                nonBl != null ? nonBl.getFinalDestName() : null,
                nonBl != null ? nonBl.getFinalEta() : null,
                nonBl != null ? mapOrNull(nonBl.getVolumeWtKg(), Weight::kg) : null,
                nonBl != null ? mapOrNull(nonBl.getRton(), Rton::ton) : null
        );
    }
}
