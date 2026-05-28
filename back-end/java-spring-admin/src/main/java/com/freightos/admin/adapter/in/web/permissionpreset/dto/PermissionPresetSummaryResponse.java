package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import java.util.List;

public record PermissionPresetSummaryResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        List<Long> attributeValueIds
) {}
