package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.ShipmentType;
import java.math.BigDecimal;

// null 필드는 기존 값 유지 정책 (PATCH 의미론)
public record UpdateHouseBlRequest(
        JobDiv jobDiv,
        Bound bound,
        String hblNo,
        ShipmentType shipmentType,
        FreightTerm freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode,
        String teamCode,
        String salesManCode,
        Long masterBlId
) {}
