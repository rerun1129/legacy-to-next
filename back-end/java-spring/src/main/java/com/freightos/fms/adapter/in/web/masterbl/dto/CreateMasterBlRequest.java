package com.freightos.fms.adapter.in.web.masterbl.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** Master B/L 신규 등록 요청 DTO. */
public record CreateMasterBlRequest(
        @NotNull String jobDiv,
        @NotNull String bound,
        @Size(max = 35) String mblNo,
        @Size(max = 35) String masterRefNo,
        @NotNull String freightTerm,
        @Size(max = 20) String shipperCode,
        String shipperAddress,
        @Size(max = 20) String consigneeCode,
        String consigneeAddress,
        @Size(max = 20) String notifyCode,
        String notifyAddress,
        @Size(max = 5)  String polCode,
        @Size(max = 5)  String podCode,
        @Pattern(regexp = "\\d{8}") String etd,
        @Pattern(regexp = "\\d{8}") String eta,
        @Min(0) Integer pkgQty,
        String pkgUnit,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        String hsCode,
        String mainItemName,
        @Size(max = 20) String settlePartnerCode,
        String operatorCode,

        // SEA 확장 필드
        SeaDetailRequest seaDetail,

        // Sub 엔티티
        DescRequest desc,
        List<DimRequest> dims,
        List<ScheduleLegRequest> scheduleLegs,
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
            String vesselNationality,
            String weightUnit,
            String serviceTerm,
            String blType,
            @Size(max = 5) String porCode,
            @Size(max = 5) String finalDestCode,
            @DecimalMin("0") BigDecimal rton,
            @Size(max = 35) String lineBkgNo,
            @Pattern(regexp = "\\d{8}") String issueDate
    ) {}

    /** 화물 표시 및 명세. MasterBl당 1건. */
    public record DescRequest(
            String marks,
            String description,
            String descClause1,
            String descClause2,
            String remark
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
