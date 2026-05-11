package com.freightos.fms.application.truckbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Truck B/L 단건 조회 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record TruckBlDetailResult(
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
}
