package com.freightos.admin.application.button.command;

public record UpdateButtonCommand(
        Long menuId,
        String label,
        String actionType,
        String apiMethod,
        String apiPath,
        Integer sortOrder,
        Boolean active
) {}
