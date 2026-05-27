package com.freightos.admin.application.customer.projection;

import com.freightos.admin.domain.customer.entity.CustomerType;

import java.time.LocalDateTime;

public record CustomerSummary(
        Long id,
        String customerCode,
        CustomerType customerType,
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String customerLocalAddress,
        String customerEnglishAddress,
        String countryCode,
        String memo,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
