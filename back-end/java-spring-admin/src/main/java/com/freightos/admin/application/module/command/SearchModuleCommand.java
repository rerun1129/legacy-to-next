package com.freightos.admin.application.module.command;

public record SearchModuleCommand(
        String moduleCode,
        String name,
        Boolean active,
        int page,
        int size
) {}
