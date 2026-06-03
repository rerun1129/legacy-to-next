package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.freight.FreightLineView;
import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.masterbl.projection.AirChargeProjection;
import com.freightos.fms.application.masterbl.projection.AirDetailProjection;
import com.freightos.fms.application.masterbl.projection.ConsoledHouseBlSummaryView;
import com.freightos.fms.application.masterbl.projection.ConsoledSeaContainerView;
import com.freightos.fms.application.masterbl.projection.DescProjection;
import com.freightos.fms.application.masterbl.projection.DimProjection;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailView;
import com.freightos.fms.application.masterbl.projection.ScheduleLegProjection;
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
        String teamName,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String mainItemName,
        String hsCode,
        String hsCodeName,
        String settlePartnerCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummaryView> consolidatedHouseBls,
        List<ConsoledSeaContainerView> consoledSeaContainers,
        String remark,
        DescView desc,
        SeaDetailResponse seaDetail,
        AirDetailResponse airDetail,
        List<DimView> dims,
        List<ScheduleLegView> scheduleLegs,
        List<AirChargeView> airCharges,

        // Freight 탭 응답 (없으면 null)
        FreightResponse freight
) {
    public static MasterBlDetailResponse from(MasterBlDetailView view) {
        MasterBlDetailResult result = view.base();
        SeaDetailProjection seaDetailProjection = result.seaDetail();
        AirDetailProjection airDetailProjection = result.airDetail();
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
                view.teamName(),
                result.pkgQty(),
                result.pkgUnit(),
                result.weightUnit(),
                result.grossWeightKg(),
                result.cbm(),
                result.mainItemName(),
                result.hsCode(),
                view.hsCodeName(),
                result.settlePartnerCode(),
                result.createdAt(),
                result.updatedAt(),
                result.consolidatedHouseBls(),
                result.consoledSeaContainers(),
                result.remark(),
                DescView.from(result.desc()),
                seaDetailProjection != null ? SeaDetailResponse.from(seaDetailProjection) : null,
                airDetailProjection != null ? AirDetailResponse.from(airDetailProjection) : null,
                result.dims() == null ? List.of() : result.dims().stream().map(DimView::from).toList(),
                result.scheduleLegs() == null ? List.of() : result.scheduleLegs().stream().map(ScheduleLegView::from).toList(),
                result.airCharges() == null ? List.of() : result.airCharges().stream().map(AirChargeView::from).toList(),
                view.freight() != null ? FreightResponse.from(view.freight()) : null
        );
    }

    /** Freight 탭 응답 DTO. */
    public record FreightResponse(
            // 환율 헤더
            String sellRateDt,
            String sellRateCurrencyCode,
            BigDecimal sellRate,
            String buyRateDt,
            String buyRateCurrencyCode,
            BigDecimal buyRate,
            String usdRateDt,
            BigDecimal usdRate,
            // 라인
            List<FreightLineResponse> selling,
            List<FreightLineResponse> buying
    ) {
        public static FreightResponse from(FreightView v) {
            List<FreightLineResponse> selling = v.lines().stream()
                    .filter(l -> "SELLING".equals(l.freightType()))
                    .map(FreightLineResponse::from)
                    .toList();
            List<FreightLineResponse> buying = v.lines().stream()
                    .filter(l -> "BUYING".equals(l.freightType()))
                    .map(FreightLineResponse::from)
                    .toList();
            return new FreightResponse(
                    v.sellRateDt(), v.sellRateCurrencyCode(), v.sellRate(),
                    v.buyRateDt(), v.buyRateCurrencyCode(), v.buyRate(),
                    v.usdRateDt(), v.usdRate(),
                    selling, buying
            );
        }
    }

    /** Freight 라인 1행 응답 DTO. */
    public record FreightLineResponse(
            Long id,
            String freightCode,
            String per,
            BigDecimal qty,
            BigDecimal price,
            String currency,
            String customerCode,
            String taxType,
            String performanceDt,
            // 계산값
            String financialDocType,
            BigDecimal exchangeRate,
            BigDecimal settleAmount,
            BigDecimal localAmount,
            BigDecimal settleTaxAmount,
            BigDecimal localTaxAmount,
            BigDecimal usdAmount
    ) {
        public static FreightLineResponse from(FreightLineView l) {
            return new FreightLineResponse(
                    l.freightLineId(),
                    l.freightCode(),
                    l.per(),
                    l.unitQuantity(),
                    l.unitPrice(),
                    l.currency(),
                    l.customerCode(),
                    l.taxType(),
                    l.performanceDt(),
                    l.financialDocType(),
                    l.exchangeRate(),
                    l.settleAmount(),
                    l.localAmount(),
                    l.settleTaxAmount(),
                    l.localTaxAmount(),
                    l.usdAmount()
            );
        }
    }

    /**
     * AIR 본체 상세 응답 뷰. AirDetailProjection을 1:1 매핑한다.
     * desc는 root MasterBlDetailResponse.DescView로 통합되어 이 뷰에서 제외된다 (§6.49 ㉕).
     * AIR은 container 미사용 — container 필드 없음 (§13.9).
     */
    public record AirDetailResponse(
            String airlineCode,
            BigDecimal chargeWeightKg,
            BigDecimal volumeWeightKg,
            String rateClass,
            String currencyCode,
            String declaredValueCarriage,
            String declaredValueCustoms,
            String insurance,
            String accountInformation,
            String securityStatus,
            String flightType,
            String issueDate,
            String issuePlace,
            String signature,
            String otherTerm,
            String handlingInfoCode,
            String handlingInfoText,
            String remark
    ) {
        /** §6.55 — nested object null 방지용 empty 팩토리. */
        public static AirDetailResponse empty() {
            return new AirDetailResponse(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        }

        public static AirDetailResponse from(AirDetailProjection p) {
            return new AirDetailResponse(
                    p.airlineCode(), p.chargeWeightKg(), p.volumeWeightKg(), p.rateClass(),
                    p.currencyCode(), p.declaredValueCarriage(), p.declaredValueCustoms(),
                    p.insurance(), p.accountInformation(), p.securityStatus(), p.flightType(),
                    p.issueDate(), p.issuePlace(), p.signature(), p.otherTerm(),
                    p.handlingInfoCode(), p.handlingInfoText(), p.remark()
            );
        }
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

    /** AIR Dim(치수) 행 응답 뷰. */
    public record DimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {
        public static DimView from(DimProjection p) {
            return new DimView(p.id(), p.lengthCm(), p.widthCm(), p.heightCm(), p.quantity(), p.cbm(), p.volumeWeightKg());
        }
    }

    /** AIR Schedule Leg(구간 일정) 행 응답 뷰. */
    public record ScheduleLegView(
            Long id,
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
    ) {
        public static ScheduleLegView from(ScheduleLegProjection p) {
            return new ScheduleLegView(p.id(), p.toCode(), p.byCarrier(), p.flightNo(), p.onBoardDt(), p.onBoardTm(), p.arrivalDt(), p.arrivalTm());
        }
    }

    /** AIR Charge 행 응답 뷰. */
    public record AirChargeView(
            Long id,
            String freightCode,
            String currencyCode,
            String per,
            String freightTerm,
            BigDecimal grossWeightKg,
            String rateClass,
            BigDecimal chargeWeightKg,
            BigDecimal rate
    ) {
        public static AirChargeView from(AirChargeProjection p) {
            return new AirChargeView(p.id(), p.freightCode(), p.currencyCode(), p.per(), p.freightTerm(), p.grossWeightKg(), p.rateClass(), p.chargeWeightKg(), p.rate());
        }
    }
}
