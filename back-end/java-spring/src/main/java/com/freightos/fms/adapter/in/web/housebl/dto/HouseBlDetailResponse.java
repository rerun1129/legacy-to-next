package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.HouseBlDetailView;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** House B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record HouseBlDetailResponse(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String blType,
        String freightTerm,
        String shipperCode,
        String shipperName,
        String shipperAddress,
        String consigneeCode,
        String consigneeName,
        String consigneeAddress,
        String notifyCode,
        String notifyName,
        String notifyAddress,
        String docPartnerCode,
        String docPartnerName,
        String docPartnerAddress,
        String polCode,
        String polName,
        String podCode,
        String podName,
        String deliveryCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String actualCustomerCode,
        String actualCustomerName,
        String operatorCode,
        String operatorName,
        String teamCode,
        String teamName,
        String salesManCode,
        String salesManName,
        Long masterBlId,
        String mblNo,
        String masterRefNo,
        String settlePartnerCode,
        String settlePartnerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // 거래 조건
        String incoterms,
        String salesClass,
        String hsCode,
        String hsCodeName,

        // Non B/L 전용 필드
        String originalBlRef,
        String workDivision,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,

        // SEA/TRUCK 본체 loadType (AIR/Non B/L은 null)
        String loadType,

        // SEA/AIR/TRUCK 본체 remark
        String remark,

        // SEA 본체 상세 (AIR/TRUCK/NON_BL은 null)
        SeaDetailResponse seaDetail,

        // AIR 본체 상세 (SEA/TRUCK/NON_BL은 null)
        AirDetailResponse airDetail
) {
    public static HouseBlDetailResponse from(HouseBlDetailView view) {
        HouseBlDetailResult r = view.base();
        return new HouseBlDetailResponse(
                r.id(),
                r.hblNo(),
                r.jobDiv(),
                r.bound(),
                r.shipmentType(),
                r.blType(),
                r.freightTerm(),
                r.shipperCode(),
                view.shipperName(),
                r.shipperAddress(),
                r.consigneeCode(),
                view.consigneeName(),
                r.consigneeAddress(),
                r.notifyCode(),
                view.notifyName(),
                r.notifyAddress(),
                r.docPartnerCode(),
                view.docPartnerName(),
                r.docPartnerAddress(),
                r.polCode(),
                view.polName(),
                r.podCode(),
                view.podName(),
                r.deliveryCode(),
                r.etd(),
                r.eta(),
                r.pkgQty(),
                r.pkgUnit(),
                r.weightUnit(),
                r.grossWeightKg(),
                r.cbm(),
                r.actualCustomerCode(),
                view.actualCustomerName(),
                r.operatorCode(),
                view.operatorName(),
                r.teamCode(),
                view.teamName(),
                r.salesManCode(),
                view.salesManName(),
                r.masterBlId(),
                r.mblNo(),
                r.masterRefNo(),
                r.settlePartnerCode(),
                view.settlePartnerName(),
                r.createdAt(),
                r.updatedAt(),
                r.incoterms(),
                r.salesClass(),
                r.hsCode(),
                view.hsCodeName(),
                r.originalBlRef(),
                r.workDivision(),
                r.linerCode(),
                r.linerName(),
                r.vesselName(),
                r.voyageNo(),
                r.finalDestCode(),
                r.finalDestName(),
                r.finalEta(),
                r.volumeWeightKg(),
                r.rton(),
                r.loadType(),
                r.remark(),
                r.seaDetail() != null
                        ? SeaDetailResponse.from(r.seaDetail(), view.issuePlaceName(), view.payableAtName(), view.deliveryName(), view.linerName())
                        : null,
                r.airDetail() != null ? AirDetailResponse.from(r.airDetail()) : null
        );
    }
}
