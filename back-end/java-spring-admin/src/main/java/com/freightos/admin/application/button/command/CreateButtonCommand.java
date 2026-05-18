package com.freightos.admin.application.button.command;

public record CreateButtonCommand(
        String buttonCode,
        Long menuId,
        String label,
        String actionType,
        String apiMethod,
        String apiPath,
        Integer sortOrder,
        Boolean active
) {}
