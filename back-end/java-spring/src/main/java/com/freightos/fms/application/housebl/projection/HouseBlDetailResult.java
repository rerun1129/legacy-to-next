package com.freightos.fms.application.housebl.projection;

import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * House B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * domain 레이어의 enum은 application→domain 방향으로 허용된다.
 */
public record HouseBlDetailResult(
        Long id,
        String hblNo,
        JobDiv jobDiv,
        Bound bound,
        ShipmentType shipmentType,
        BlType blType,
        FreightTerm freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String docPartnerCode,
        String polCode,
        String podCode,
        String deliveryCode,
        String etd,
        String eta,
        Integer pkgQty,
        WeightUnit pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String actualCustomerCode,
        String operatorCode,
        String teamCode,
        String salesManCode,
        Long masterBlId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

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
        BigDecimal rton
) {
}
