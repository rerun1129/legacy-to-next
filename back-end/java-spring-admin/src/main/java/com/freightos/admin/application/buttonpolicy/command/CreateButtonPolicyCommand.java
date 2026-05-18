package com.freightos.admin.application.buttonpolicy.command;

public record CreateButtonPolicyCommand(
        Long buttonId,
        String attributeKey,
        String requiredValue
) {}
