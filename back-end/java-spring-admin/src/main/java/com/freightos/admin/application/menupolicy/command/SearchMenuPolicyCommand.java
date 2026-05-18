package com.freightos.admin.application.menupolicy.command;

public record SearchMenuPolicyCommand(
        Long menuId,
        String attributeKey,
        String requiredValue,
        int page,
        int size
) {}
