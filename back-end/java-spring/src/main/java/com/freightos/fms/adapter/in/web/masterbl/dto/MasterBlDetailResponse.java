package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Master B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record MasterBlDetailResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        MasterBlJobDiv jobDiv,
        Bound bound,
        ShipmentType shipmentType,
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
        WeightUnit pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummary> consolidatedHouseBls
) {
    public static MasterBlDetailResponse from(MasterBlDetailResult result) {
        return new MasterBlDetailResponse(
                result.id(),
                result.mblNo(),
                result.masterRefNo(),
                result.jobDiv(),
                result.bound(),
                result.shipmentType(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.polCode(),
                result.podCode(),
                result.etd(),
                result.eta(),
                result.freightTerm(),
                result.operatorCode(),
                result.teamCode(),
                result.pkgQty(),
                result.pkgUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.createdAt(),
                result.updatedAt(),
                result.consolidatedHouseBls()
        );
    }
}
