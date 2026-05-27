package com.freightos.admin.adapter.in.web.code.country.dto;

import java.time.LocalDateTime;

public record CountryDetailResponse(
        Long id,
        String countryCode,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
