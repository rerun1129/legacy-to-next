package com.freightos.admin.common.security;

/**
 * 정책 평가 단위 — attribute_key + required_value 쌍.
 * 같은 key 내 OR, 다른 key 간 AND 로 평가.
 */
public record PolicyRow(String attributeKey, String requiredValue) {}
