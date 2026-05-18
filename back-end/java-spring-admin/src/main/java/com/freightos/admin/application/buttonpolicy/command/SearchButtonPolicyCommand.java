package com.freightos.admin.application.buttonpolicy.command;

public record SearchButtonPolicyCommand(
        Long buttonId,
        String attributeKey,
        String requiredValue,
        int page,
        int size
) {}
