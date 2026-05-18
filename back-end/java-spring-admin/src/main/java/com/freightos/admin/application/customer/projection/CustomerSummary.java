package com.freightos.admin.application.customer.projection;

import com.freightos.admin.domain.customer.entity.CustomerType;

import java.time.LocalDateTime;

public record CustomerSummary(
        Long id,
        String customerCode,
        CustomerType customerType,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
