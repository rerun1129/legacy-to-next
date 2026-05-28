package com.freightos.admin.application.permissionpreset.projection;

import java.util.List;

/**
 * 프리셋 상세 조회 프로젝션.
 * attributeValueIds 외에 각 attribute_value 의 key/value/label 을 포함한다.
 */
public record PermissionPresetDetail(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<Long> attributeValueIds,
        List<AttributeValueItem> attributeValues
) {
    public record AttributeValueItem(Long id, String attributeKey, String value, String label) {}
}
