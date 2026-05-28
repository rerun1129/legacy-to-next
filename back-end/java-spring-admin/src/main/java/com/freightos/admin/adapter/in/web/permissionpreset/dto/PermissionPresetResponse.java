package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import java.util.List;

public record PermissionPresetResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<Long> attributeValueIds,
        List<AttributeValueRef> attributeValues
) {}
