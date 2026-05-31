package com.freightos.fms.adapter.in.web.truckbl.dto;

import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailView;

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
        String teamName,
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

        // 거래 조건
        String hsCode,
        String hsCodeName,

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
    ) {
        /** desc 행이 아직 없을 때 FE zod object 스키마 통과용 빈 인스턴스를 반환한다. */
        public static DescView empty() {
            return new DescView(null, null, null, null);
        }
    }

    public static TruckBlDetailResponse from(TruckBlDetailView view) {
        TruckBlDetailResult result = view.base();
        List<TruckOrderView> truckOrderViews = result.truckOrders() == null ? null
                : result.truckOrders().stream()
                        .map(o -> new TruckOrderView(
                                o.id(), o.truckOrderNo(), o.pkgQty(), o.pkgUnit(),
                                o.grossWeightKg(), o.cbm(),
                                o.truckNo(), o.truckType(), o.driver(), o.mobileNo(),
                                o.containerNo(), o.containerType(),
                                o.sealNo1(), o.sealNo2(), o.sealNo3()))
                        .toList();
        DescView descView = result.desc() == null ? DescView.empty()
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
                view.teamName(),
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
                result.hsCode(),
                view.hsCodeName(),
                result.remark(),
                truckOrderViews,
                descView,
                result.volumeDivisor(),
                result.dims() == null ? null : result.dims().stream()
                        .map(d -> new DimView(d.id(), d.lengthCm(), d.widthCm(), d.heightCm(), d.quantity(), d.cbm(), d.volumeWeightKg()))
                        .toList()
        );
    }
}
