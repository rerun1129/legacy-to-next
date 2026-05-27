package com.freightos.admin.application.code.country.projection;

import java.time.LocalDateTime;

public record CountrySummary(
        Long id,
        String countryCode,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
