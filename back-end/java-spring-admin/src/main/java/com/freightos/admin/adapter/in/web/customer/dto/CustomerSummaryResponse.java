package com.freightos.admin.adapter.in.web.customer.dto;

import com.freightos.admin.domain.customer.entity.CustomerType;

import java.time.LocalDateTime;

public record CustomerSummaryResponse(
        Long id,
        String customerCode,
        CustomerType customerType,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
