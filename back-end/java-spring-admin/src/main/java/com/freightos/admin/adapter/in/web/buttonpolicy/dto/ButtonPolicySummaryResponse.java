package com.freightos.admin.adapter.in.web.buttonpolicy.dto;

import java.time.LocalDateTime;

public record ButtonPolicySummaryResponse(
        Long id,
        Long buttonId,
        String attributeKey,
        String requiredValue,
        LocalDateTime updatedAt
) {}
