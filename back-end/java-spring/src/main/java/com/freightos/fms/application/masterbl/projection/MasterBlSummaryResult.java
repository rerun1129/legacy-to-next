package com.freightos.fms.application.masterbl.projection;

import java.time.LocalDateTime;

/**
 * N+1 문제 해소를 위한 리스트 조회 전용 projection.
 * QueryDSL Projections.constructor 로 직접 채워지며,
 * application 경계에서 String으로 통일됨.
 */
public record MasterBlSummaryResult(
    Long id,
    String mblNo,
    String masterRefNo,
    String jobDiv,
    String bound,
    String shipperCode,
    String consigneeCode,
    String polCode,
    String podCode,
    String etd,
    String eta,
    String operatorCode,
    LocalDateTime createdAt
) {}
