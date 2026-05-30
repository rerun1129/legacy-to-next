package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * House B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record HouseBlDetailResult(
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
        String mblNo,
        String masterRefNo,
        String settlePartnerCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // 거래 조건
        String incoterms,
        String salesClass,
        String hsCode,

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

        // SEA/TRUCK 본체 loadType (AIR/Non B/L은 null)
        String loadType,

        // SEA/AIR/TRUCK 본체 remark
        String remark,

        // SEA 본체 상세 (AIR/TRUCK/NON_BL은 null)
        SeaDetailProjection seaDetail,

        // AIR 본체 상세 (SEA/TRUCK/NON_BL은 null)
        AirDetailProjection airDetail
) {
}
