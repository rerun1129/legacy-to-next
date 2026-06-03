package com.freightos.fms.adapter.in.web.housebl.dto;

import java.math.BigDecimal;
import java.util.List;

// null 필드는 기존 값 유지 정책 (PATCH 의미론)
public record UpdateHouseBlRequest(
        String jobDiv,
        String bound,
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
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,

        // SEA/AIR/TRUCK 본체 remark
        String remark,

        // SEA 확장 필드
        SeaDetailRequest seaDetail,

        // AIR 확장 필드
        AirDetailRequest airDetail,

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
     * Freight 필드 없는 53-인자 편의 생성자 — 기존 테스트 호환성 유지용.
     * freight 필드는 모두 null로 초기화된다.
     */
    public UpdateHouseBlRequest(
            String jobDiv, String bound, String shipmentType, String freightTerm,
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
        this(jobDiv, bound, shipmentType, freightTerm,
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
            String onboardDate,
            String porCode,
            String finalDestCode,
            String issueDate,
            String noOfBl,
            String issuePlace,
            String doDate,
            String payableAt,
            Boolean triangle,
            String serviceTerm,
            String vesselNationality,
            BigDecimal rton,
            String sayInformation,
            String noOfContainerOrPackages,
            String blType,
            String deliveryCode
    ) {}

    /** AIR 모드 확장 필드. PATCH 의미론 — 모든 필드 nullable. */
    public record AirDetailRequest(
            String airlineCode,
            BigDecimal chargeWeightKg,
            BigDecimal volumeWeightKg,
            String rateClass,
            String currencyCode,
            String declaredValueCarriage,
            String declaredValueCustoms,
            String insurance,
            String accountInformation,
            String otherTerm,
            String issueDate,
            String issuePlace,
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

    /** 포장 치수 명세 1행. UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record DimRequest(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    /** 컨테이너 배정 1행. UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record ContainerRequest(
            Long id,
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

    /** 구간별 운항 스케줄 1행. UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record ScheduleLegRequest(
            Long id,
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
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

    /** AIR Charge 1행. UPDATE 전용 — 기존 행 식별을 위한 id 포함. */
    public record AirChargeRequest(
            Long id,
            String freightCode,
            String currencyCode,
            String per,
            String freightTerm,
            BigDecimal grossWeightKg,
            String rateClass,
            BigDecimal chargeWeightKg,
            BigDecimal rate
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
            String performanceDt,
            String exchangeRate,
            String settleAmount,
            String localAmount,
            String usdExchangeRate,
            String usdAmount,
            String localTaxAmount,
            String financialDocType
    ) {}
}
