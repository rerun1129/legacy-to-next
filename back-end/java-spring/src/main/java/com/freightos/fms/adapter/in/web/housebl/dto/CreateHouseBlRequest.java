package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.adapter.in.web.validation.AirGroup;
import com.freightos.fms.adapter.in.web.validation.AirImpGroup;
import com.freightos.fms.adapter.in.web.validation.SeaGroup;
import com.freightos.fms.adapter.in.web.validation.SeaImpGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateHouseBlRequest(
        @NotNull String jobDiv,
        @NotNull @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String bound,
        @Size(max = 35) @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String hblNo,
        @NotNull @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String shipmentType,
        @NotNull String freightTerm,
        @Size(max = 20) String shipperCode,
        String shipperAddress,
        @Size(max = 20) @NotBlank(groups = {SeaImpGroup.class, AirImpGroup.class}) String consigneeCode,
        String consigneeAddress,
        @Size(max = 20) String notifyCode,
        String notifyAddress,
        @Size(max = 20) @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String docPartnerCode,
        String docPartnerAddress,
        @Size(max = 20) String settlePartnerCode,
        @Size(max = 5) @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String polCode,
        @Size(max = 5) @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String podCode,
        @Pattern(regexp = "\\d{8}") @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String etd,
        @Pattern(regexp = "\\d{8}") @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String eta,
        @Min(0) Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        @Size(max = 20) @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String actualCustomerCode,
        @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String operatorCode,
        @NotBlank(groups = {SeaGroup.class, AirGroup.class}) String teamCode,
        @NotBlank(groups = AirGroup.class) String salesManCode,
        Long masterBlId,
        String incoterms,
        String salesClass,
        String mainItemName,
        String hsCode,
        @Size(max = 35) String mblNo,
        @Size(max = 35) String masterRefNo,

        // Non B/L 전용 필드
        String workDivision,
        String originalBlRef,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        @Pattern(regexp = "\\d{8}") String finalEta,
        @DecimalMin("0") BigDecimal volumeWeightKg,
        @DecimalMin("0") BigDecimal rton,

        // SEA/AIR/TRUCK 본체 remark
        String remark,

        // SEA 확장 필드
        SeaDetailRequest seaDetail,

        // AIR 확장 필드
        @Valid AirDetailRequest airDetail,

        // Sub 엔티티
        DescRequest desc,
        List<DimRequest> dims,
        List<ContainerRequest> containers,
        List<ScheduleLegRequest> scheduleLegs,
        List<TruckOrderRequest> truckOrders,
        List<AirChargeRequest> airCharges
) {

    /** SEA 모드 확장 필드. */
    public record SeaDetailRequest(
            String loadType,
            String linerCode,
            String vesselCode,
            String vesselName,
            String voyageNo,
            @Pattern(regexp = "\\d{8}") String onboardDate,
            @Size(max = 5) String porCode,
            @Size(max = 5) String finalDestCode,
            @Pattern(regexp = "\\d{8}") String issueDate,
            String noOfBl,
            @Size(max = 5) String issuePlace,
            @Pattern(regexp = "\\d{8}") String doDate,
            @Size(max = 5) String payableAt,
            Boolean triangle,
            String serviceTerm,
            String vesselCode2,
            String vesselNationality,
            @DecimalMin("0") BigDecimal rton,
            String sayInformation,
            String noOfContainerOrPackages,
            String blType,
            @Size(max = 5) String deliveryCode
    ) {}

    /** AIR 모드 확장 필드. */
    public record AirDetailRequest(
            @NotBlank(groups = AirGroup.class) String airlineCode,
            @DecimalMin("0") BigDecimal chargeWeightKg,
            @DecimalMin("0") BigDecimal volumeWeightKg,
            String rateClass,
            String currencyCode,
            String declaredValueCarriage,
            String declaredValueCustoms,
            String insurance,
            String accountInformation,
            String otherTerm,
            @Pattern(regexp = "\\d{8}") String issueDate,
            @Size(max = 5) String issuePlace,
            String signature,
            String fhd,
            String handlingInformationCode,
            String handlingInformationDesc,
            String originOfGoods,
            String cargoType
    ) {}

    /** 화물 표시 및 명세. HouseBl당 1건. */
    public record DescRequest(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    /** 포장 치수 명세 1행. */
    public record DimRequest(
            @DecimalMin("0") BigDecimal lengthCm,
            @DecimalMin("0") BigDecimal widthCm,
            @DecimalMin("0") BigDecimal heightCm,
            @Min(0) Integer quantity,
            @DecimalMin("0") BigDecimal cbm,
            @DecimalMin("0") BigDecimal volumeWeightKg
    ) {}

    /** 컨테이너 배정 1행. */
    public record ContainerRequest(
            String containerNo,
            String containerType,
            Integer lengthFeet,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            String sealNo4,
            String sealNo5,
            String sealNo6,
            @Min(0) Integer pkgQty,
            String pkgUnit,
            @DecimalMin("0") BigDecimal grossWeightKg,
            @DecimalMin("0") BigDecimal netWeightKg,
            @DecimalMin("0") BigDecimal cbm,
            @DecimalMin("0") BigDecimal vgmKg,
            Boolean soc,
            Integer seq
    ) {}

    /** 구간별 운항 스케줄 1행. */
    public record ScheduleLegRequest(
            String toCode,
            String byCarrier,
            String flightNo,
            @Pattern(regexp = "\\d{8}") String onBoardDt,
            String onBoardTm,
            @Pattern(regexp = "\\d{8}") String arrivalDt,
            String arrivalTm
    ) {}

    /** 트럭 오더 1행. */
    public record TruckOrderRequest(
            String truckOrderNo,
            @Min(0) Integer pkgQty,
            String pkgUnit,
            @DecimalMin("0") BigDecimal grossWeightKg,
            @DecimalMin("0") BigDecimal cbm,
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

    /** AIR Charge 1행. */
    public record AirChargeRequest(
            String freightCode,
            String currencyCode,
            String per,
            String freightTerm,
            @DecimalMin("0") BigDecimal grossWeightKg,
            String rateClass,
            @DecimalMin("0") BigDecimal chargeWeightKg,
            @DecimalMin("0") BigDecimal rate
    ) {}
}
