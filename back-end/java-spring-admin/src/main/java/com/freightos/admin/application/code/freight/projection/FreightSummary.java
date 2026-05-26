package com.freightos.admin.application.code.freight.projection;

import java.time.LocalDateTime;

public record FreightSummary(
        Long id,
        String freightCode,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
