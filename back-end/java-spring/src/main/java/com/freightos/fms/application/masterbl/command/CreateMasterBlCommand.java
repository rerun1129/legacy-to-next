package com.freightos.fms.application.masterbl.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * Master B/L 생성 커맨드. DTO→도메인 변환 책임을 Application 계층(Factory)으로 격리하기 위한 중간 표현.
 * validation 어노테이션 없음 — 검증은 Request DTO에서 수행 완료.
 */
public record CreateMasterBlCommand(
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
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String hsCode,
        String mainItemName,
        String settlePartnerCode,
        String operatorCode,

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
            String weightUnit,
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
            String descClause2,
            String remark
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
