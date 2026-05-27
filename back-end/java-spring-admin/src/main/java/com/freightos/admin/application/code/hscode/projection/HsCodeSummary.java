package com.freightos.admin.application.code.hscode.projection;

import java.time.LocalDateTime;

public record HsCodeSummary(
        Long id,
        String hsCode,
        String name,
        String nameEn,
        String countryCode,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
