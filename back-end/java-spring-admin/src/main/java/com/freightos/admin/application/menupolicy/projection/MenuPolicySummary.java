package com.freightos.admin.application.menupolicy.projection;

import java.time.LocalDateTime;

public record MenuPolicySummary(
        Long id,
        Long menuId,
        String attributeKey,
        String requiredValue,
        LocalDateTime updatedAt
) {}
