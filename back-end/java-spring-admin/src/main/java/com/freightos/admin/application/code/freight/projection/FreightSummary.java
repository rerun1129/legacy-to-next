package com.freightos.admin.application.code.freight.projection;

import java.time.LocalDateTime;

public record FreightSummary(
        Long id,
        String freightCode,
        String name,
        String nameEn,
        String description,
        String freightUnit,
        String freightGroup,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
