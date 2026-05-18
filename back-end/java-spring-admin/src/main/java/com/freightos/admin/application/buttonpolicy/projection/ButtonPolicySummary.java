package com.freightos.admin.application.buttonpolicy.projection;

import java.time.LocalDateTime;

public record ButtonPolicySummary(
        Long id,
        Long buttonId,
        String attributeKey,
        String requiredValue,
        LocalDateTime updatedAt
) {}
