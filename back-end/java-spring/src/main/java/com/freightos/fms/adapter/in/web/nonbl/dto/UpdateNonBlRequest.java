package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

// null 필드는 기존 값 유지 정책 (PATCH 의미론)
public record UpdateNonBlRequest(
        String jobDiv,
        @NotBlank String bound,
        String workDivision,
        String originalBlRef,
        String volumeDivisor,
        String salesClass,
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
        String mainItemName,
        String hsCode,
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
        CreateHouseBlRequest.SeaDetailRequest seaDetail,

        // Sub 엔티티 — NonBl UPDATE 전용 (id 포함하여 merge-by-id 지원)
        List<DimRequest> dims,
        List<ContainerRequest> containers,
        List<CreateHouseBlRequest.ScheduleLegRequest> scheduleLegs,
        List<CreateHouseBlRequest.TruckOrderRequest> truckOrders,
        List<CreateHouseBlRequest.AirChargeRequest> airCharges
) {

    /** NonBl UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record DimRequest(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    /** NonBl UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record ContainerRequest(
            Long id,
            String containerNo,
            String containerType,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal cbm
    ) {}
}
