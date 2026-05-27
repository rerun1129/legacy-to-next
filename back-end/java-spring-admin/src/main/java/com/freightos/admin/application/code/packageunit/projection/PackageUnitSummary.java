package com.freightos.admin.application.code.packageunit.projection;

import java.time.LocalDateTime;

public record PackageUnitSummary(
        Long id,
        String packageCode,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
