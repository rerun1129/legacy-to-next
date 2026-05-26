package com.freightos.admin.adapter.in.web.code.packageunit.dto;

import java.time.LocalDateTime;

public record PackageUnitSummaryResponse(
        Long id,
        String packageCode,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
