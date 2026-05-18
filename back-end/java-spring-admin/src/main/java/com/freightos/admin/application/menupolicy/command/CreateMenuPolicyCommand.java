package com.freightos.admin.application.menupolicy.command;

public record CreateMenuPolicyCommand(
        Long menuId,
        String attributeKey,
        String requiredValue
) {}
