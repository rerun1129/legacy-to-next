package com.freightos.admin.adapter.in.web.module.dto;

import java.time.LocalDateTime;

public record ModuleDetailResponse(
        Long id,
        String moduleCode,
        String name,
        String description,
        Integer sortOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
