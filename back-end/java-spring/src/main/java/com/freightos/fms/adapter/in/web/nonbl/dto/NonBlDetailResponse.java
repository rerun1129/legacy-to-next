package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Non B/L 단건 조회/생성/수정 응답 DTO.
 * NonBlDetailResult projection을 그대로 미러한다.
 */
public record NonBlDetailResponse(
        Long id,
        String hblNo,
        String bound,
        String workDivision,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String settlePartnerCode,
        String actualCustomerCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        String volumeDivisor,
        String originalBlRef,
        BigDecimal rton,
        BigDecimal volumeWtKg,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode,
        String salesManCode,
        String teamCode,
        String teamName,
        String salesClass,
        String mainItemName,
        String hsCode,
        String hsCodeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String remark,
        List<ContainerView> containers,
        List<DimView> dims
) {

    public record ContainerView(
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

    public record DimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {}

    public static NonBlDetailResponse from(NonBlDetailView view) {
        NonBlDetailResult result = view.base();
        return new NonBlDetailResponse(
                result.id(),
                result.hblNo(),
                result.bound(),
                result.workDivision(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.settlePartnerCode(),
                result.actualCustomerCode(),
                result.polCode(),
                result.podCode(),
                result.etd(),
                result.eta(),
                result.linerCode(),
                result.linerName(),
                result.vesselName(),
                result.voyageNo(),
                result.finalDestCode(),
                result.finalDestName(),
                result.finalEta(),
                result.volumeDivisor(),
                result.originalBlRef(),
                result.rton(),
                result.volumeWtKg(),
                result.pkgQty(),
                result.pkgUnit(),
                result.weightUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.operatorCode(),
                result.salesManCode(),
                result.teamCode(),
                view.teamName(),
                result.salesClass(),
                result.mainItemName(),
                result.hsCode(),
                view.hsCodeName(),
                result.createdAt(),
                result.updatedAt(),
                result.remark(),
                toContainerViews(result.containers()),
                toDimViews(result.dims())
        );
    }

    private static List<ContainerView> toContainerViews(List<NonBlDetailResult.NonBlContainerView> src) {
        if (src == null) return List.of();
        return src.stream().map(c -> new ContainerView(
                c.id(), c.containerNo(), c.containerType(),
                c.sealNo1(), c.sealNo2(), c.sealNo3(),
                c.pkgQty(), c.pkgUnit(), c.grossWeightKg(), c.cbm())).toList();
    }

    private static List<DimView> toDimViews(List<NonBlDetailResult.NonBlDimView> src) {
        if (src == null) return List.of();
        return src.stream().map(d -> new DimView(
                d.id(), d.lengthCm(), d.widthCm(), d.heightCm(),
                d.quantity(), d.cbm(), d.volumeWeightKg())).toList();
    }
}
