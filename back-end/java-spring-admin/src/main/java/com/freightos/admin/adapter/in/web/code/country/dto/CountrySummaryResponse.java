package com.freightos.admin.adapter.in.web.code.country.dto;

import java.time.LocalDateTime;

public record CountrySummaryResponse(
        Long id,
        String countryCode,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
