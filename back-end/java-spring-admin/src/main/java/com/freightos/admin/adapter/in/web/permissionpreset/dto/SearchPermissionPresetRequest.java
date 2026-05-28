package com.freightos.admin.adapter.in.web.permissionpreset.dto;

public record SearchPermissionPresetRequest(
        String code,
        String name,
        Boolean activeOnly
) {}
