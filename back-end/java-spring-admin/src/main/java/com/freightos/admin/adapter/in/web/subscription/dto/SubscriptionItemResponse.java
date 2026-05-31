package com.freightos.admin.adapter.in.web.subscription.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionItemResponse(
        Long id,
        Long subscriberId,
        String moduleCode,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
