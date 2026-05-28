package com.freightos.admin.application.permissionpreset.command;

public record UpdatePermissionPresetCommand(
        String name,
        String description,
        boolean active
) {}
