package com.freightos.admin.application.subscription.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionSummary(
        Long id,
        Long subscriberId,
        String moduleCode,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
