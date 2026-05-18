package com.freightos.admin.application.module.command;

public record CreateModuleCommand(
        String moduleCode,
        String name,
        String description,
        Integer sortOrder,
        Boolean active
) {}
