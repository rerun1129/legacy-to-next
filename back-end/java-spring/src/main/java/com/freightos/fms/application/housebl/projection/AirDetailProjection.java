package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;
import java.util.List;

/**
 * AIR 본체(HouseBlAir) 상세 필드의 application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record AirDetailProjection(
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
        List<AirScheduleLegProjection> scheduleLegs,
        List<AirChargeProjection> airCharges,
        List<AirDimProjection> dims,
        AirDescProjection desc
) {}
