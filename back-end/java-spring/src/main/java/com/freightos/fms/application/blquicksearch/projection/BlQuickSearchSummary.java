package com.freightos.fms.application.blquicksearch.projection;

/**
 * House/Master BL 통합 자동완성 조회 결과 projection.
 * QueryDSL Projections.constructor로 직접 채워진다.
 * blType: "HOUSE" 또는 "MASTER" (Expressions.constant로 주입).
 */
public record BlQuickSearchSummary(
    Long id,
    String blType,
    String blNo,
    String jobDiv,
    String bound,
    String shipperCode,
    String polCode,
    String podCode,
    String etd
) {}
