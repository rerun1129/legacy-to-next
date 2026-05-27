package com.freightos.admin.application.code.freight.projection;

import com.freightos.admin.domain.code.freight.FreightGroup;

import java.time.LocalDateTime;

public record FreightSummary(
        Long id,
        String freightCode,
        String name,
        String nameEn,
        String description,
        String freightUnit,
        FreightGroup freightGroup,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
