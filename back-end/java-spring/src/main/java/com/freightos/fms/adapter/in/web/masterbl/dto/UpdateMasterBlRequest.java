package com.freightos.fms.adapter.in.web.masterbl.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Master B/L 수정 요청 DTO.
 * 모든 필드는 nullable — null 은 기존 값 유지(PATCH 의미론).
 * mblNo·masterRefNo는 이 DTO에서 제외 — ChangeMasterBlNoCommand 전용 경로(PUT /{id}/mbl-no)로만 변경 가능.
 * 내부 record는 CreateMasterBlRequest와 구조 동일, import 사이클 방지를 위해 별도 정의.
 */
public record UpdateMasterBlRequest(
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

    /** 화물 표시 및 명세. MasterBl당 1건. */
    public record DescRequest(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {}

    /** 포장 치수 명세 1행. */
    public record DimRequest(
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    /** 구간별 운항 스케줄 1행. */
    public record ScheduleLegRequest(
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
    ) {}

    /** AIR Charge 1행. */
    public record AirChargeRequest(
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
