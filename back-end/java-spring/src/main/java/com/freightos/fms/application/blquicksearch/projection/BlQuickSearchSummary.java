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
    String etd,
    String eta
) {
    /** 하위호환: eta 미지정(9-인자) 호출부 보존 (기존 테스트 등). 코더는 테스트를 수정하지 말 것. */
    public BlQuickSearchSummary(Long id, String blType, String blNo, String jobDiv, String bound,
                                String shipperCode, String polCode, String podCode, String etd) {
        this(id, blType, blNo, jobDiv, bound, shipperCode, polCode, podCode, etd, null);
    }
}
