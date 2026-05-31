package com.freightos.fms.application.nonbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Non B/L 리스트 응답 전용 projection.
 * NonBlSummary(코드만) + code→name 1개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 NonBlSummaryResponse DTO로 변환한다.
 */
public record NonBlListItem(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String shipperCode,
        String consigneeCode,
        Integer pkgQty,
        String pkgUnit,
        LocalDateTime createdAt,
        String notifyCode,
        String settlePartnerCode,
        String actualCustomerCode,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String vesselName,
        String voyageNo,
        String linerCode,
        String linerName,
        String teamCode,
        String teamName
) {}
