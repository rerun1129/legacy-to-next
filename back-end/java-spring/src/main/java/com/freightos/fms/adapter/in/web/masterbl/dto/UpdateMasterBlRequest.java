package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.math.BigDecimal;

/** Master B/L 수정 요청 DTO. 모든 필드는 nullable(부분 수정 허용). */
public record UpdateMasterBlRequest(
        MasterBlJobDiv jobDiv,
        Bound bound,
        String mblNo,
        String masterRefNo,
        FreightTerm freightTerm,
        String shipperCode,
        String consigneeCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        Integer pkgQty,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode
) {}
