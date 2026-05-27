package com.freightos.admin.adapter.in.web.code.hscode.dto;

import java.time.LocalDateTime;

public record HsCodeDetailResponse(
        Long id,
        String hsCode,
        String name,
        String nameEn,
        String countryCode,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
