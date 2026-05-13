package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** House B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record HouseBlDetailResponse(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String blType,
        String freightTerm,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        String docPartnerCode,
        String docPartnerAddress,
        String polCode,
        String podCode,
        String deliveryCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
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
        BigDecimal rton,

        // SEA/AIR/TRUCK 본체 remark
        String remark
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
                result.shipperAddress(),
                result.consigneeCode(),
                result.consigneeAddress(),
                result.notifyCode(),
                result.notifyAddress(),
                result.docPartnerCode(),
                result.docPartnerAddress(),
                result.polCode(),
                result.podCode(),
                result.deliveryCode(),
                result.etd(),
                result.eta(),
                result.pkgQty(),
                result.pkgUnit(),
                result.weightUnit(),
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
                result.rton(),
                result.remark()
        );
    }
}
