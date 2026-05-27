package com.freightos.admin.adapter.in.web.code.freight.dto;

import java.time.LocalDateTime;

public record FreightDetailResponse(
        Long id,
        String freightCode,
        String name,
        String nameEn,
        String description,
        String freightUnit,
        String freightGroup,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
