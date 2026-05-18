package com.freightos.admin.application.module.command;

public record UpdateModuleCommand(
        String name,
        String description,
        Integer sortOrder,
        Boolean active
) {}
