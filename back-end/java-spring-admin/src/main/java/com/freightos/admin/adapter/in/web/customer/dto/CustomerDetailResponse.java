package com.freightos.admin.adapter.in.web.customer.dto;

import com.freightos.admin.domain.customer.entity.CustomerType;

import java.time.LocalDateTime;

public record CustomerDetailResponse(
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
        String memo,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
