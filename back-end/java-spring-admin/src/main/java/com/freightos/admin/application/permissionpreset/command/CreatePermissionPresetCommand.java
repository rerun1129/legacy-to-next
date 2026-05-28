package com.freightos.admin.application.permissionpreset.command;

public record CreatePermissionPresetCommand(
        String code,
        String name,
        String description,
        boolean active
) {}
