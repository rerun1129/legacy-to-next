package com.freightos.fms.adapter.in.web.truckbl.dto;

import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Truck B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record TruckBlDetailResponse(
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
        DescView desc
) {
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

    public static TruckBlDetailResponse from(TruckBlDetailResult result) {
        List<TruckOrderView> truckOrderViews = result.truckOrders() == null ? null
                : result.truckOrders().stream()
                        .map(o -> new TruckOrderView(
                                o.id(), o.truckOrderNo(), o.pkgQty(), o.pkgUnit(),
                                o.grossWeightKg(), o.cbm(),
                                o.truckNo(), o.truckType(), o.driver(), o.mobileNo(),
                                o.containerNo(), o.containerType(),
                                o.sealNo1(), o.sealNo2(), o.sealNo3()))
                        .toList();
        DescView descView = result.desc() == null ? null
                : new DescView(
                        result.desc().marks(), result.desc().description(),
                        result.desc().descClause1(), result.desc().descClause2());
        return new TruckBlDetailResponse(
                result.id(),
                result.hblNo(),
                result.jobDiv(),
                result.bound(),
                result.shipmentType(),
                result.freightTerm(),
                result.shipperCode(),
                result.shipperAddr(),
                result.consigneeCode(),
                result.consigneeAddr(),
                result.notifyCode(),
                result.notifyAddr(),
                result.settlePartnerCode(),
                result.docPartnerCode(),
                result.docPartnerAddress(),
                result.polCode(),
                result.podCode(),
                result.deliveryCode(),
                result.etd(),
                result.eta(),
                result.pkgQty(),
                result.pkgUnit(),
                result.weightUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.actualCustomerCode(),
                result.operatorCode(),
                result.teamCode(),
                result.salesManCode(),
                result.incoterms(),
                result.createdAt(),
                result.updatedAt(),
                result.truckerCode(),
                result.truckerPic(),
                result.chargeWeightKg(),
                result.pickupDate(),
                result.pickupTm(),
                result.etdTm(),
                result.etaTm(),
                result.loadType(),
                result.serviceTerm(),
                result.vesselName(),
                result.voyageNo(),
                result.remark(),
                truckOrderViews,
                descView
        );
    }
}
