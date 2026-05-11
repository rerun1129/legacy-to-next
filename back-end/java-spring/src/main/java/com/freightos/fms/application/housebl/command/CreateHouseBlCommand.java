package com.freightos.fms.application.housebl.command;

import java.math.BigDecimal;
import java.util.List;

/**
 * House B/L 생성 커맨드. DTO→도메인 변환 책임을 Application 계층(Factory)으로 격리하기 위한 중간 표현.
 * validation 어노테이션 없음 — 검증은 Request DTO에서 수행 완료.
 */
public record CreateHouseBlCommand(
        String jobDiv,
        String bound,
        String hblNo,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        String docPartnerCode,
        String docPartnerAddress,
        String settlePartnerCode,
        String polCode,
        String podCode,
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
        String incoterms,
        String salesClass,
        String mainItemName,
        String hsCode,
        String mblNo,
        String masterRefNo,

        // Non B/L 전용 필드
        String workDivision,
        String originalBlRef,
        String volumeDivisor,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,
        String remark,

        // SEA 확장 필드
        SeaDetailCommand seaDetail,

        // Sub 엔티티
        DescCommand desc,
        List<DimCommand> dims,
        List<ContainerCommand> containers,
        List<ScheduleLegCommand> scheduleLegs,
        List<TruckOrderCommand> truckOrders,
        List<AirChargeCommand> airCharges
) {

    public record SeaDetailCommand(
            String loadType,
            String linerCode,
            String vesselCode,
            String vesselName,
            String voyageNo,
            String onboardDate,
            String porCode,
            String finalDestCode,
            String issueDate,
            Integer noOfBl,
            String issuePlace,
            String doDate,
            String payableAt,
            Boolean triangle,
            String serviceTerm,
            String vesselCode2,
            String vesselNationality,
            BigDecimal rton,
            String sayInformation,
            String noOfContainerOrPackages,
            String blType,
            String deliveryCode
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

    public record ContainerCommand(
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
            BigDecimal cbm,
            BigDecimal vgmKg,
            Boolean soc,
            Integer seq
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

    public record TruckOrderCommand(
            String truckOrderNo,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal cbm,
            String truckNo,
            String truckType,
            String driver,
            String mobileNo,
            String containerNo,
            String containerType,
            String sealNo1,
            String sealNo2,
            String sealNo3
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
