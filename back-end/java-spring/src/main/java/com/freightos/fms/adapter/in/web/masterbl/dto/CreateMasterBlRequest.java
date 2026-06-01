package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.adapter.in.web.validation.AirImpMasterGroup;
import com.freightos.fms.adapter.in.web.validation.AirMasterGroup;
import com.freightos.fms.adapter.in.web.validation.SeaImpMasterGroup;
import com.freightos.fms.adapter.in.web.validation.SeaMasterGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** Master B/L 신규 등록 요청 DTO. */
public record CreateMasterBlRequest(
        @NotNull String jobDiv,
        @NotNull String bound,
        @Size(max = 35) @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String mblNo,
        @Size(max = 35) @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String masterRefNo,
        String freightTerm,
        @Size(max = 20) String shipperCode,
        String shipperAddress,
        @Size(max = 20) @NotBlank(groups = {SeaImpMasterGroup.class, AirImpMasterGroup.class}) String consigneeCode,
        String consigneeAddress,
        @Size(max = 20) String notifyCode,
        String notifyAddress,
        @Size(max = 5)  @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String polCode,
        @Size(max = 5)  @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String podCode,
        @Pattern(regexp = "\\d{8}") @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String etd,
        @Pattern(regexp = "\\d{8}") @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String eta,
        @Min(0) Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        String hsCode,
        String mainItemName,
        @Size(max = 20) String settlePartnerCode,
        @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String operatorCode,
        @NotBlank(groups = {SeaMasterGroup.class, AirMasterGroup.class}) String teamCode,
        String shipmentType,
        String remark,

        // SEA 확장 필드
        @Valid SeaDetailRequest seaDetail,

        // AIR 확장 필드
        @Valid AirDetailRequest airDetail,

        // Sub 엔티티
        DescRequest desc,
        List<DimRequest> dims,
        List<ScheduleLegRequest> scheduleLegs,
        List<AirChargeRequest> airCharges
) {

    /** SEA 모드 확장 필드. */
    public record SeaDetailRequest(
            String loadType,
            @NotBlank(groups = SeaMasterGroup.class) String linerCode,
            String vesselCode,
            @NotBlank(groups = SeaMasterGroup.class) String vesselName,
            @NotBlank(groups = SeaMasterGroup.class) String voyageNo,
            @Pattern(regexp = "\\d{8}") String onboardDate,
            String vesselNationality,
            String serviceTerm,
            String blType,
            @Size(max = 5) String porCode,
            @Size(max = 5) String finalDestCode,
            @DecimalMin("0") BigDecimal rton,
            @Size(max = 35) String lineBkgNo,
            @Pattern(regexp = "\\d{8}") String issueDate
    ) {}

    /** AIR 모드 확장 필드. */
    public record AirDetailRequest(
            @NotBlank(groups = AirMasterGroup.class) String airlineCode,
            @DecimalMin("0") BigDecimal chargeWeightKg,
            @DecimalMin("0") BigDecimal volumeWeightKg,
            String rateClass,
            String currencyCode,
            String declaredValueCarriage,
            String declaredValueCustoms,
            String insurance,
            String accountInformation,
            String securityStatus,
            String flightType,
            @Pattern(regexp = "\\d{8}") String issueDate,
            String issuePlace,
            String signature,
            String otherTerm,
            String handlingInfoCode,
            String handlingInfoText,
            String remark
    ) {}

    /** 화물 표시 및 명세. MasterBl당 1건. */
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
