package com.freightos.admin.adapter.in.web.menupolicy.dto;

import java.time.LocalDateTime;

public record MenuPolicySummaryResponse(
        Long id,
        Long menuId,
        String attributeKey,
        String requiredValue,
        LocalDateTime updatedAt
) {}
