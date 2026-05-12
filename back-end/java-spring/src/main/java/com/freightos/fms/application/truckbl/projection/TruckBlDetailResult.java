package com.freightos.fms.application.truckbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Truck B/L 단건 조회 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record TruckBlDetailResult(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String shipperAddr,
        String consigneeCode,
        String consigneeAddr,
        String notifyCode,
        String notifyAddr,
        String settlePartnerCode,
        String docPartnerCode,
        String docPartnerAddress,
        String polCode,
        String podCode,
        String deliveryCode,
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
        String incoterms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Truck 전용 필드
        String truckerCode,
        String truckerPic,
        BigDecimal chargeWeightKg,
        String pickupDate,
        String pickupTm,
        String etdTm,
        String etaTm,
        String loadType,
        String serviceTerm,
        String vesselName,
        String voyageNo,

        // Marks/Description 패널
        String remark,

        // 자식 데이터
        List<TruckOrderView> truckOrders,
        DescView desc,

        // Dimension 그리드
        String volumeDivisor,
        List<DimView> dims
) {
    public record DimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    public record TruckOrderView(
            Long id,
            String truckOrderNo, Integer pkgQty, String pkgUnit,
            BigDecimal grossWeightKg, BigDecimal cbm,
            String truckNo, String truckType, String driver, String mobileNo,
            String containerNo, String containerType,
            String sealNo1, String sealNo2, String sealNo3
    ) {}

    public record DescView(
            String marks, String description,
            String descClause1, String descClause2
    ) {}
}
