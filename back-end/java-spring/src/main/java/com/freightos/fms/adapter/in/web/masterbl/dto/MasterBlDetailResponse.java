package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.ConsoledSeaContainerView;
import com.freightos.fms.application.masterbl.projection.DescProjection;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Master B/L 상세 응답 DTO. 도메인 엔티티를 직접 노출하지 않는다. */
public record MasterBlDetailResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String shipperAddress,
        String consigneeAddress,
        String notifyAddress,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String freightTerm,
        String operatorCode,
        String teamCode,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String mainItemName,
        String hsCode,
        String settlePartnerCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummaryView> consolidatedHouseBls,
        List<ConsoledSeaContainerView> consoledSeaContainers,
        String remark,
        DescView desc,
        SeaDetailResponse seaDetail
) {
    public static MasterBlDetailResponse from(MasterBlDetailResult result) {
        SeaDetailProjection seaDetailProjection = result.seaDetail();
        return new MasterBlDetailResponse(
                result.id(),
                result.mblNo(),
                result.masterRefNo(),
                result.jobDiv(),
                result.bound(),
                result.shipmentType(),
                result.shipperCode(),
                result.consigneeCode(),
                result.notifyCode(),
                result.shipperAddress(),
                result.consigneeAddress(),
                result.notifyAddress(),
                result.polCode(),
                result.podCode(),
                result.etd(),
                result.eta(),
                result.freightTerm(),
                result.operatorCode(),
                result.teamCode(),
                result.pkgQty(),
                result.pkgUnit(),
                result.weightUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.mainItemName(),
                result.hsCode(),
                result.settlePartnerCode(),
                result.createdAt(),
                result.updatedAt(),
                result.consolidatedHouseBls(),
                result.consoledSeaContainers(),
                result.remark(),
                DescView.from(result.desc()),
                seaDetailProjection != null ? SeaDetailResponse.from(seaDetailProjection) : null
        );
    }

    /** desc(master_bl_desc) 응답 뷰. SEA/AIR 공통으로 root 레벨에 노출된다. */
    public record DescView(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {
        public static DescView from(DescProjection p) {
            if (p == null) return null;
            return new DescView(p.marks(), p.description(), p.descClause1(), p.descClause2());
        }
    }
}
