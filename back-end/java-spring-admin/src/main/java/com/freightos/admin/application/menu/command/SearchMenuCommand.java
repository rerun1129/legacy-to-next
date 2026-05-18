package com.freightos.admin.application.menu.command;

public record SearchMenuCommand(
        String menuCode,
        String label,
        String moduleCode,
        Long parentId,
        Boolean active,
        int page,
        int size
) {}
