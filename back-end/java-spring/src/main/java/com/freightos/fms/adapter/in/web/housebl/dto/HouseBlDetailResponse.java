package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.enums.JobDiv;

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
    public static HouseBlDetailResponse from(HouseBlDetailResult result) {
        return new HouseBlDetailResponse(
                result.id(),
                result.hblNo(),
                result.jobDiv(),
                result.bound(),
                result.shipmentType(),
                result.blType(),
                result.freightTerm(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.docPartnerCode(),
                result.polCode(),
                result.podCode(),
                result.deliveryCode(),
                result.etd(),
                result.eta(),
                result.pkgQty(),
                result.pkgUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.actualCustomerCode(),
                result.operatorCode(),
                result.teamCode(),
                result.salesManCode(),
                result.masterBlId(),
                result.createdAt(),
                result.updatedAt(),
                result.originalBlRef(),
                result.workDivision(),
                result.linerCode(),
                result.linerName(),
                result.vesselName(),
                result.voyageNo(),
                result.finalDestCode(),
                result.finalDestName(),
                result.finalEta(),
                result.volumeWeightKg(),
                result.rton()
        );
    }
}
