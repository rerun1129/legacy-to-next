package com.freightos.admin.application.permissionpreset.projection;

import java.util.List;

public record PermissionPresetSummary(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<Long> attributeValueIds
) {}
