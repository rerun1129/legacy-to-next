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
        List<AirChargeRequest> airCharges,

        // Freight 탭 — 환율 헤더 (FE와 동일 필드명)
        String sellRateDt,
        String sellRateCurrencyCode,
        String sellRate,
        String buyRateDt,
        String buyRateCurrencyCode,
        String buyRate,
        String usdRateDt,
        String usdRate,

        // Freight 탭 — 매출/매입 라인
        List<FreightLineRequest> freightSelling,
        List<FreightLineRequest> freightBuying
) {

    /**
     * Freight 필드 없는 54-인자 편의 생성자 — 기존 테스트 호환성 유지용.
     * freight 필드는 모두 null로 초기화된다.
     */
    public CreateHouseBlRequest(
            String jobDiv, String bound, String hblNo, String shipmentType, String freightTerm,
            String shipperCode, String shipperAddress, String consigneeCode, String consigneeAddress,
            String notifyCode, String notifyAddress, String docPartnerCode, String docPartnerAddress,
            String settlePartnerCode, String polCode, String podCode, String etd, String eta,
            Integer pkgQty, String pkgUnit, String weightUnit,
            BigDecimal grossWeightKg, BigDecimal cbm,
            String actualCustomerCode, String operatorCode, String teamCode, String salesManCode,
            Long masterBlId, String incoterms, String salesClass, String mainItemName, String hsCode,
            String mblNo, String masterRefNo,
            String workDivision, String originalBlRef,
            String linerCode, String linerName, String vesselName, String voyageNo,
            String finalDestCode, String finalDestName, String finalEta,
            BigDecimal volumeWeightKg, BigDecimal rton, String remark,
            SeaDetailRequest seaDetail, AirDetailRequest airDetail,
            DescRequest desc, List<DimRequest> dims, List<ContainerRequest> containers,
            List<ScheduleLegRequest> scheduleLegs, List<TruckOrderRequest> truckOrders,
            List<AirChargeRequest> airCharges) {
        this(jobDiv, bound, hblNo, shipmentType, freightTerm,
                shipperCode, shipperAddress, consigneeCode, consigneeAddress,
                notifyCode, notifyAddress, docPartnerCode, docPartnerAddress,
                settlePartnerCode, polCode, podCode, etd, eta,
                pkgQty, pkgUnit, weightUnit, grossWeightKg, cbm,
                actualCustomerCode, operatorCode, teamCode, salesManCode,
                masterBlId, incoterms, salesClass, mainItemName, hsCode,
                mblNo, masterRefNo,
                workDivision, originalBlRef,
                linerCode, linerName, vesselName, voyageNo,
                finalDestCode, finalDestName, finalEta,
                volumeWeightKg, rton, remark,
                seaDetail, airDetail,
                desc, dims, containers, scheduleLegs, truckOrders, airCharges,
                null, null, null, null, null, null, null, null, null, null);
    }

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

    /** Freight 매출/매입 라인 1행. FE FREIGHT_ROW_SCHEMA와 필드명 동일. */
    public record FreightLineRequest(
            Long id,
            String freightCode,
            String per,
            String qty,
            String price,
            String currency,
            String customerCode,
            String taxType,
            String performanceDt
    ) {}
}
