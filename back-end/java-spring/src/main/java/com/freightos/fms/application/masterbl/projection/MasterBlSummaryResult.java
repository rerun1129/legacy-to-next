package com.freightos.fms.application.masterbl.projection;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.time.LocalDateTime;

/**
 * N+1 문제 해소를 위한 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * 도메인 계층에만 의존하므로 ArchUnit DOMAIN_MUST_NOT_DEPEND_ON_ADAPTERS 통과.
 */
public record MasterBlSummaryResult(
    Long id,
    String mblNo,
    String masterRefNo,
    MasterBlJobDiv jobDiv,
    Bound bound,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etd,
    String eta,
    String operatorCode,
    LocalDateTime createdAt
) {}
