package com.freightos.fms.application.masterbl.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * Master B/L 수정 커맨드. null 필드는 기존 값 유지(PATCH 의미론).
 * mblNo·masterRefNo는 제외 — ChangeMasterBlNoCommand 전용 경로로만 변경 가능.
 */
public record UpdateMasterBlCommand(
        String jobDiv,
        String bound,
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

        // AIR 확장 필드
        AirDetailCommand airDetail,

        // Sub 엔티티
        DescCommand desc,
        List<DimCommand> dims,
        List<ScheduleLegCommand> scheduleLegs,
        List<AirChargeCommand> airCharges,

        // Freight 탭 커맨드 (null이면 freight 미포함 저장)
        FreightCommand freight
) {

    /**
     * freight 미지정 편의 생성자 — freight=null로 위임.
     * 기존 Assembler의 positional 호출을 보존한다.
     */
    public UpdateMasterBlCommand(
            String jobDiv, String bound, String freightTerm,
            String shipperCode, String shipperAddress, String consigneeCode, String consigneeAddress,
            String notifyCode, String notifyAddress, String polCode, String podCode, String etd, String eta,
            Integer pkgQty, String pkgUnit, String weightUnit, BigDecimal grossWeightKg, BigDecimal cbm,
            String hsCode, String mainItemName, String settlePartnerCode, String operatorCode, String teamCode,
            String shipmentType, String remark,
            SeaDetailCommand seaDetail, AirDetailCommand airDetail,
            DescCommand desc, List<DimCommand> dims, List<ScheduleLegCommand> scheduleLegs,
            List<AirChargeCommand> airCharges) {
        this(jobDiv, bound, freightTerm,
                shipperCode, shipperAddress, consigneeCode, consigneeAddress,
                notifyCode, notifyAddress, polCode, podCode, etd, eta,
                pkgQty, pkgUnit, weightUnit, grossWeightKg, cbm,
                hsCode, mainItemName, settlePartnerCode, operatorCode, teamCode,
                shipmentType, remark,
                seaDetail, airDetail,
                desc, dims, scheduleLegs, airCharges, null);
    }

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

    public record AirDetailCommand(
            String airlineCode,
            BigDecimal chargeWeightKg,
            BigDecimal volumeWeightKg,
            String rateClass,
            String currencyCode,
            String declaredValueCarriage,
            String declaredValueCustoms,
            String insurance,
            String accountInformation,
            String securityStatus,
            String flightType,
            String issueDate,
            String issuePlace,
            String signature,
            String otherTerm,
            String handlingInfoCode,
            String handlingInfoText,
            String remark
    ) {}

    public record DescCommand(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    public record DimCommand(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    public record ScheduleLegCommand(
            Long id,
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
    ) {}

    public record AirChargeCommand(
            Long id,
            String freightCode,
            String currencyCode,
            String per,
            String freightTerm,
            BigDecimal grossWeightKg,
            String rateClass,
            BigDecimal chargeWeightKg,
            BigDecimal rate
    ) {}

    /** Freight 탭 헤더+라인 커맨드. null이면 freight 저장 생략. */
    public record FreightCommand(
            String sellRateDt,
            String sellRateCurrencyCode,
            BigDecimal sellRate,
            String buyRateDt,
            String buyRateCurrencyCode,
            BigDecimal buyRate,
            String usdRateDt,
            BigDecimal usdRate,
            List<FreightLineCommand> selling,
            List<FreightLineCommand> buying
    ) {}

    /** Freight 라인 1행 커맨드. FE 실시간 계산값 포함. */
    public record FreightLineCommand(
            String freightCode,
            String per,
            BigDecimal unitQuantity,
            BigDecimal unitPrice,
            String currency,
            String customerCode,
            String taxType,
            String performanceDt,
            BigDecimal exchangeRate,
            BigDecimal settleAmount,
            BigDecimal localAmount,
            BigDecimal usdExchangeRate,
            BigDecimal usdAmount,
            BigDecimal localTaxAmount,
            /** 금융 서류 종류 (name() 문자열). null/blank면 FinancialDocTypePolicy 폴백. */
            String financialDocType
    ) {

        /** 하위호환 생성자 — 계산값 미포함. 계산값은 null로 초기화. */
        public FreightLineCommand(String freightCode, String per, BigDecimal unitQuantity, BigDecimal unitPrice,
                String currency, String customerCode, String taxType, String performanceDt) {
            this(freightCode, per, unitQuantity, unitPrice, currency, customerCode, taxType, performanceDt,
                    null, null, null, null, null, null, null);
        }
    }
}
