package com.freightos.admin.adapter.out.persistence.menupolicy;

/**
 * ABAC 평가용 JOIN 쿼리 결과 투영.
 * 메뉴 row 당 정책이 없는 경우 getAttributeKey()/getRequiredValue() 는 null.
 */
public interface MenuPolicyEvalProjection {
    Long getMenuId();
    String getMenuCode();
    String getAttributeKey();
    String getRequiredValue();
}
