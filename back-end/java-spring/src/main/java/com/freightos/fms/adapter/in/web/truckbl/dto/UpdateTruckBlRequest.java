package com.freightos.fms.adapter.in.web.truckbl.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

/**
 * Truck B/L 수정 요청 DTO.
 * hblNo 필드 포함 금지 — B/L 번호 변경은 PUT /{id}/hbl-no 전용 endpoint 사용.
 * null 필드는 기존 값 유지 정책 (PATCH 의미론).
 */
public record UpdateTruckBlRequest(
        @NotBlank String bound,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
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

        // Truck Order 그리드 (UPDATE — id 필수, §6.28 자식 row merge-by-id)
        List<TruckOrderRequest> truckOrders,

        // Dimension 그리드 (UPDATE — id 포함, 기존 행 식별)
        List<DimRequest> dims
) {

    /** Marks/Description 패널 1건. id 포함으로 기존 행 식별. */
    public record DescRequest(
            Long id,
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    /** Dimension 그리드 행 (UPDATE — id 포함, 기존 행 식별). */
    public record DimRequest(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    /** Truck Order 그리드 행 (UPDATE — id 포함, 기존 행 식별). */
    public record TruckOrderRequest(
            Long id,
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
