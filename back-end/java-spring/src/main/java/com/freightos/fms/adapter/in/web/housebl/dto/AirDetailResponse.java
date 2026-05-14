package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.AirChargeProjection;
import com.freightos.fms.application.housebl.projection.AirDescProjection;
import com.freightos.fms.application.housebl.projection.AirDetailProjection;
import com.freightos.fms.application.housebl.projection.AirDimProjection;
import com.freightos.fms.application.housebl.projection.AirScheduleLegProjection;

import java.math.BigDecimal;
import java.util.List;

/** AIR 본체 상세 응답 DTO. AirDetailProjection을 1:1 매핑한다. */
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
        String otherTerm,
        String issueDate,
        String issuePlace,
        String signature,
        String fhd,
        String handlingInformationCode,
        String handlingInformationDesc,
        String originOfGoods,
        String cargoType,
        List<AirScheduleLegView> scheduleLegs,
        List<AirChargeView> airCharges,
        List<AirDimView> dims,
        AirDescView desc
) {
    public static AirDetailResponse from(AirDetailProjection p) {
        return new AirDetailResponse(
                p.airlineCode(),
                p.chargeWeightKg(),
                p.volumeWeightKg(),
                p.rateClass(),
                p.currencyCode(),
                p.declaredValueCarriage(),
                p.declaredValueCustoms(),
                p.insurance(),
                p.accountInformation(),
                p.otherTerm(),
                p.issueDate(),
                p.issuePlace(),
                p.signature(),
                p.fhd(),
                p.handlingInformationCode(),
                p.handlingInformationDesc(),
                p.originOfGoods(),
                p.cargoType(),
                p.scheduleLegs() == null ? List.of() : p.scheduleLegs().stream().map(AirScheduleLegView::from).toList(),
                p.airCharges() == null ? List.of() : p.airCharges().stream().map(AirChargeView::from).toList(),
                p.dims() == null ? List.of() : p.dims().stream().map(AirDimView::from).toList(),
                p.desc() != null ? AirDescView.from(p.desc()) : AirDescView.empty()
        );
    }

    public record AirScheduleLegView(
            Long id,
            String toCode,
            String byCarrier,
            String flightNo,
            String onBoardDt,
            String onBoardTm,
            String arrivalDt,
            String arrivalTm
    ) {
        public static AirScheduleLegView empty() {
            return new AirScheduleLegView(null, null, null, null, null, null, null, null);
        }

        public static AirScheduleLegView from(AirScheduleLegProjection p) {
            return new AirScheduleLegView(p.id(), p.toCode(), p.byCarrier(), p.flightNo(),
                    p.onBoardDt(), p.onBoardTm(), p.arrivalDt(), p.arrivalTm());
        }
    }

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
        public static AirChargeView empty() {
            return new AirChargeView(null, null, null, null, null, null, null, null, null);
        }

        public static AirChargeView from(AirChargeProjection p) {
            return new AirChargeView(p.id(), p.freightCode(), p.currencyCode(), p.per(),
                    p.freightTerm(), p.grossWeightKg(), p.rateClass(), p.chargeWeightKg(), p.rate());
        }
    }

    public record AirDimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {
        public static AirDimView empty() {
            return new AirDimView(null, null, null, null, null, null, null);
        }

        public static AirDimView from(AirDimProjection p) {
            return new AirDimView(p.id(), p.lengthCm(), p.widthCm(), p.heightCm(),
                    p.quantity(), p.cbm(), p.volumeWeightKg());
        }
    }

    public record AirDescView(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {
        public static AirDescView empty() {
            return new AirDescView(null, null, null, null);
        }

        public static AirDescView from(AirDescProjection p) {
            return new AirDescView(p.marks(), p.description(), p.descClause1(), p.descClause2());
        }
    }
}
