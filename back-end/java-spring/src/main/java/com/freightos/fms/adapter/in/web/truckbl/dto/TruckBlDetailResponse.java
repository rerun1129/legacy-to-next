package com.freightos.fms.adapter.in.web.truckbl.dto;

import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Truck B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record TruckBlDetailResponse(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String settlePartnerCode,
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
        String incoterms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Truck 전용 필드
        String truckerCode,
        String truckerPic,
        BigDecimal chargeWeightKg,
        String pickupDate,
        String pickupTm,
        String etdTm,
        String etaTm,
        String loadType,
        String serviceTerm,
        String voyageNo
) {
    public static TruckBlDetailResponse from(TruckBlDetailResult result) {
        return new TruckBlDetailResponse(
                result.id(),
                result.hblNo(),
                result.jobDiv(),
                result.bound(),
                result.shipmentType(),
                result.freightTerm(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.settlePartnerCode(),
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
                result.incoterms(),
                result.createdAt(),
                result.updatedAt(),
                result.truckerCode(),
                result.truckerPic(),
                result.chargeWeightKg(),
                result.pickupDate(),
                result.pickupTm(),
                result.etdTm(),
                result.etaTm(),
                result.loadType(),
                result.serviceTerm(),
                result.voyageNo()
        );
    }
}
