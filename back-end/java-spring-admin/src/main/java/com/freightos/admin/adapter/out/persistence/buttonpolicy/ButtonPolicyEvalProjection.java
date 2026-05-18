package com.freightos.admin.adapter.out.persistence.buttonpolicy;

/**
 * ABAC 평가용 JOIN 쿼리 결과 투영.
 * 버튼 row 당 정책이 없는 경우 getAttributeKey()/getRequiredValue() 는 null.
 */
public interface ButtonPolicyEvalProjection {
    Long getButtonId();
    String getButtonCode();
    String getAttributeKey();
    String getRequiredValue();
}
