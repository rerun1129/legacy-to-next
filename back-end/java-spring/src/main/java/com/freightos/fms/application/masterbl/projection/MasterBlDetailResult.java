package com.freightos.fms.application.masterbl.projection;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.ShipmentType;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Master B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * domain 레이어의 enum은 application→domain 방향으로 허용된다.
 */
public record MasterBlDetailResult(
        Long id,
        String mblNo,
        String masterRefNo,
        MasterBlJobDiv jobDiv,
        Bound bound,
        ShipmentType shipmentType,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        FreightTerm freightTerm,
        String operatorCode,
        String teamCode,
        Integer pkgQty,
        WeightUnit pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummary> consolidatedHouseBls
) {}
