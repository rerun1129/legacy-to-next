package com.freightos.admin.adapter.in.web.userpermissionpreset.dto;

public record UserPermissionPresetResponse(
        Long id,
        Long userId,
        Long presetId,
        String presetCode,
        String presetName,
        boolean presetActive
) {}
