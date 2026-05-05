package com.freightos.fms.domain.truckbl.projection;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Truck B/L 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * 도메인 계층에만 의존하므로 JPA 어노테이션·Q-class 의존 없음.
 */
public record TruckBlSummary(
    Long houseBlId,
    String hblNo,
    JobDiv jobDiv,
    Bound bound,
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
    LocalDateTime createdAt
) {}
