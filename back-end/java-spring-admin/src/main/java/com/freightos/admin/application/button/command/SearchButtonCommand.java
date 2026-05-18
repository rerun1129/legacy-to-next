package com.freightos.admin.application.button.command;

public record SearchButtonCommand(
        Long menuId,
        String buttonCode,
        String label,
        String actionType,
        Boolean active,
        int page,
        int size
) {}
