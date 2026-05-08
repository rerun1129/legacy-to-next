package com.freightos.fms.application.nonbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Non B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record NonBlDetailResult(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String workDivision,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String docPartnerCode,
        String settlePartnerCode,
        String actualCustomerCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        String originalBlRef,
        BigDecimal rton,
        BigDecimal volumeWtKg,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode,
        String salesManCode,
        String teamCode,
        String mblNo,
        String masterRefNo,
        Long masterBlId,
        String mainItemName,
        String hsCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NonBlContainerView> containers,
        List<NonBlDimView> dims,
        NonBlDescView desc
) {

    public record NonBlContainerView(
            Long id,
            int seq,
            String containerNo,
            String containerType,
            Integer lengthFeet,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            String sealNo4,
            String sealNo5,
            String sealNo6,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal netWeightKg,
            BigDecimal vgmKg,
            BigDecimal cbm,
            boolean isSoc
    ) {}

    public record NonBlDimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    public record NonBlDescView(
            String marks,
            String description,
            String remark
    ) {}
}
