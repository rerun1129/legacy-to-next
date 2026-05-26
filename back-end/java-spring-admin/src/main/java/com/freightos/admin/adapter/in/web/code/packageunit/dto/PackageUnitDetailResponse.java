package com.freightos.admin.adapter.in.web.code.packageunit.dto;

import java.time.LocalDateTime;

public record PackageUnitDetailResponse(
        Long id,
        String packageCode,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
