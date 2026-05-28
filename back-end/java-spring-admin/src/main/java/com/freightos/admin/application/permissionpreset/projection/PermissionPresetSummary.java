package com.freightos.admin.application.permissionpreset.projection;

import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;

import java.util.List;

public record PermissionPresetSummary(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<AttributeValueRef> attributeValueRefs
) {}
