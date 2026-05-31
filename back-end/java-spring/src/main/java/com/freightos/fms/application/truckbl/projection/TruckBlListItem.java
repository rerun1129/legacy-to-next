package com.freightos.fms.application.truckbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Truck B/L 리스트 응답 전용 projection.
 * TruckBlSummary(코드만) + code→name 1개 name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 TruckBlSummaryResponse DTO로 변환한다.
 */
public record TruckBlListItem(
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
        String notifyCode,
        String docPartnerCode,
        String truckerCode,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        LocalDateTime createdAt,
        String teamCode,
        String teamName
) {}
