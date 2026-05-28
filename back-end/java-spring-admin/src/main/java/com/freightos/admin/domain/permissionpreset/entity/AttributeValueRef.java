package com.freightos.admin.domain.permissionpreset.entity;

/**
 * permission_preset 이 보유하는 attribute_value 참조 (attribute_key + value 복합키).
 * attribute_value 테이블의 PK 가 (attribute_key, value) 이므로 두 필드를 함께 관리한다.
 */
public record AttributeValueRef(String attributeKey, String value) {}
