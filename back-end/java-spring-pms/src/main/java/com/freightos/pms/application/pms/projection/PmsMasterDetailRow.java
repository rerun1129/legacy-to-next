package com.freightos.pms.application.pms.projection;

/**
 * master_bl 키 조회 결과 한 행. masterBlId 기준 keyed 조회.
 * B/L 식별 정보(mblNo/jobDiv/bound/etd/eta/pol/pod)를 포함.
 */
public record PmsMasterDetailRow(
    Long masterBlId,
    String mblNo,
    String jobDiv,
    String bound,
    String etd,
    String eta,
    String polCode,
    String podCode
) {}
