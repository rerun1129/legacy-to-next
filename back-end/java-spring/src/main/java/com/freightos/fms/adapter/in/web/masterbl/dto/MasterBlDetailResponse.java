package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.freightos.fms.common.util.VoMapper.mapOrNull;

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
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummary> consolidatedHouseBls
) {
    public static MasterBlDetailResponse from(MasterBlDetail detail) {
        MasterBl entity = detail.masterBl();
        return new MasterBlDetailResponse(
                entity.getId(),
                mapOrNull(entity.getMblNo(), BlNumber::value),
                mapOrNull(entity.getMasterRefNo(), BlNumber::value),
                mapOrNull(entity.getJobDiv(), MasterBlJobDiv::name),
                entity.getBound(),
                mapOrNull(entity.getShipperCode(), CustomerCode::value),
                mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                mapOrNull(entity.getPolCode(), PortCode::value),
                mapOrNull(entity.getPodCode(), PortCode::value),
                mapOrNull(entity.getEtd(), BlDate::asString),
                mapOrNull(entity.getEta(), BlDate::asString),
                entity.getFreightTerm(),
                mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                mapOrNull(entity.getTeamCode(), TeamCode::value),
                mapOrNull(entity.getPkgQty(), Quantity::count),
                mapOrNull(entity.getPkgUnit(), WeightUnit::name),
                mapOrNull(entity.getGrossWeightKg(), Weight::kg),
                mapOrNull(entity.getCbm(), Volume::cbm),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                detail.consolidatedHouseBls()
        );
    }
}
