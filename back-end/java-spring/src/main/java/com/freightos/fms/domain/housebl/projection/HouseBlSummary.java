package com.freightos.fms.domain.housebl.projection;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.time.LocalDateTime;

/**
 * N+1 문제 해소를 위한 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * 도메인 계층에만 의존하므로 ArchUnit DOMAIN_MUST_NOT_DEPEND_ON_ADAPTERS 통과.
 */
public record HouseBlSummary(
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
    Integer pkgQty,
    String pkgUnit,
    LocalDateTime createdAt
) {}
