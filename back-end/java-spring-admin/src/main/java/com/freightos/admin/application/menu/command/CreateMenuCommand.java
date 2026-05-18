package com.freightos.admin.application.menu.command;

public record CreateMenuCommand(
        String menuCode,
        Long parentId,
        String path,
        String label,
        String labelEn,
        String icon,
        Integer sortOrder,
        Boolean active,
        String moduleCode
) {}
