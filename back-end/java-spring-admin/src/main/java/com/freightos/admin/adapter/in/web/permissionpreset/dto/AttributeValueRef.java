package com.freightos.admin.adapter.in.web.permissionpreset.dto;

/**
 * 응답 DTO 내에서 프리셋에 포함된 attribute_value 요약 정보를 표현한다.
 */
public record AttributeValueRef(
        Long id,
        String attributeKey,
        String value,
        String label
) {}
