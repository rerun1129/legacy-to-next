package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.common.enums.ShipmentType;
import java.math.BigDecimal;
import java.util.List;

// null 필드는 기존 값 유지 정책 (PATCH 의미론)
public record UpdateHouseBlRequest(
        JobDiv jobDiv,
        Bound bound,
        String hblNo,
        ShipmentType shipmentType,
        FreightTerm freightTerm,
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
        WeightUnit pkgUnit,
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
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,

        // SEA 확장 필드
        SeaDetailRequest seaDetail,

        // Sub 엔티티
        DescRequest desc,
        List<DimRequest> dims,
        List<ContainerRequest> containers,
        List<ScheduleLegRequest> scheduleLegs,
        List<LicenseRequest> licenses,
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
            String vesselNationality,
            String weightUnit,
            BigDecimal rton,
            String sayInformation,
            String noOfContainerOrPackages,
            String blType,
            String deliveryCode
    ) {}

    /** 화물 표시 및 명세. HouseBl당 1건. */
    public record DescRequest(
            String marks,
            String description,
            String descClause1,
            String descClause2,
            String remark
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
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal netWeightKg,
            BigDecimal cbm,
            BigDecimal vgmKg,
            Boolean soc,
            Integer seq
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

    /** 라이선스 / 패킹 명세 1행. */
    public record LicenseRequest(
            String licenseNo,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            String combinedPackingMark,
            Integer combinedPackingQty,
            String combinedPackingUnit,
            Boolean partialShipment,
            Integer partialShipmentSeq,
            String hsnNo
    ) {}

    /** 트럭 오더 1행. */
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
