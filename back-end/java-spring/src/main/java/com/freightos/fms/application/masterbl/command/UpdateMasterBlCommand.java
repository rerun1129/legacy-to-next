package com.freightos.fms.application.masterbl.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * Master B/L 수정 커맨드. null 필드는 기존 값 유지(PATCH 의미론).
 */
public record UpdateMasterBlCommand(
        String jobDiv,
        String bound,
        String mblNo,
        String masterRefNo,
        String freightTerm,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        String polCode,
        String podCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String hsCode,
        String mainItemName,
        String settlePartnerCode,
        String operatorCode,
        String teamCode,
        String shipmentType,
        String remark,

        // SEA 확장 필드
        SeaDetailCommand seaDetail,

        // Sub 엔티티
        DescCommand desc,
        List<DimCommand> dims,
        List<ScheduleLegCommand> scheduleLegs,
        List<AirChargeCommand> airCharges
) {

    public record SeaDetailCommand(
            String loadType,
            String linerCode,
            String vesselCode,
            String vesselName,
            String voyageNo,
            String onboardDate,
            String vesselNationality,
            String serviceTerm,
            String blType,
            String porCode,
            String finalDestCode,
            BigDecimal rton,
            String lineBkgNo,
            String issueDate
    ) {}

    public record DescCommand(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    public record DimCommand(
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    public record ScheduleLegCommand(
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
    ) {}

    public record AirChargeCommand(
            String freightCode,
            String currencyCode,
            String per,
            String freightTerm,
            BigDecimal grossWeightKg,
            String rateClass,
            BigDecimal chargeWeightKg,
            BigDecimal rate
    ) {}
}
