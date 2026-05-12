package com.freightos.fms.adapter.in.web.truckbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * Truck B/L 생성 요청 DTO.
 * UI required 필드만 @NotBlank + 형식 어노테이션 적용.
 * 그 외 모든 필드: null 통과 (BE SSOT 검증 원칙).
 */
public record CreateTruckBlRequest(
        @NotBlank String bound,
        @NotBlank @Size(max = 35) String hblNo,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String settlePartnerCode,
        @NotBlank @Size(max = 5) String polCode,
        @NotBlank @Size(max = 5) String podCode,
        @NotBlank @Pattern(regexp = "\\d{8}") String etd,
        @NotBlank @Pattern(regexp = "\\d{8}") String eta,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        @NotBlank @Size(max = 20) String actualCustomerCode,
        @NotBlank @Size(max = 20) String operatorCode,
        @NotBlank @Size(max = 20) String teamCode,
        @NotBlank String salesManCode,
        String salesClass,
        String mainItemName,
        String hsCode,
        String incoterms,

        // Truck 전용 퍼포먼스 패널 필드
        String truckerCode,
        String truckerPic,
        BigDecimal chargeWeightKg,
        String pickupDate,
        String pickupTm,
        String etdTm,
        String etaTm,
        String loadType,
        String serviceTerm,
        String voyageNo,
        String volumeDivisor,

        // Marks/Description 패널
        String remark,
        DescRequest desc,

        // Truck Order 그리드
        List<TruckOrderRequest> truckOrders,

        // Dimension 그리드 (CREATE — id 없음)
        List<DimRequest> dims
) {

    /** Marks/Description 패널 1건. */
    public record DescRequest(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    /** Dimension 그리드 행 (CREATE — id 없음). */
    public record DimRequest(
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    /** Truck Order 그리드 행 (CREATE — id 없음). */
    public record TruckOrderRequest(
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
}
