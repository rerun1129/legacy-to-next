package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;

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
        String jobDiv,
        String bound,
        String workDivision,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String docPartnerCode,
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
        String salesClass,
        String mblNo,
        String masterRefNo,
        Long masterBlId,
        String mainItemName,
        String hsCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String remark,
        List<ContainerView> containers,
        List<DimView> dims
) {

    public record ContainerView(
            Long id,
            int seq,
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
            BigDecimal vgmKg,
            BigDecimal cbm,
            boolean isSoc
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

    public static NonBlDetailResponse from(NonBlDetailResult result) {
        return new NonBlDetailResponse(
                result.id(),
                result.hblNo(),
                result.jobDiv(),
                result.bound(),
                result.workDivision(),
                result.shipmentType(),
                result.freightTerm(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.docPartnerCode(),
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
                result.salesClass(),
                result.mblNo(),
                result.masterRefNo(),
                result.masterBlId(),
                result.mainItemName(),
                result.hsCode(),
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
                c.id(), c.seq(), c.containerNo(), c.containerType(), c.lengthFeet(),
                c.sealNo1(), c.sealNo2(), c.sealNo3(), c.sealNo4(), c.sealNo5(), c.sealNo6(),
                c.pkgQty(), c.pkgUnit(), c.grossWeightKg(), c.netWeightKg(), c.vgmKg(), c.cbm(), c.isSoc())).toList();
    }

    private static List<DimView> toDimViews(List<NonBlDetailResult.NonBlDimView> src) {
        if (src == null) return List.of();
        return src.stream().map(d -> new DimView(
                d.id(), d.lengthCm(), d.widthCm(), d.heightCm(),
                d.quantity(), d.cbm(), d.volumeWeightKg())).toList();
    }
}
