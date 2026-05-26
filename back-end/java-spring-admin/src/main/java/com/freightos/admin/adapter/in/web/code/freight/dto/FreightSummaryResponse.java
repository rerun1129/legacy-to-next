package com.freightos.admin.adapter.in.web.code.freight.dto;

import java.time.LocalDateTime;

public record FreightSummaryResponse(
        Long id,
        String freightCode,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
