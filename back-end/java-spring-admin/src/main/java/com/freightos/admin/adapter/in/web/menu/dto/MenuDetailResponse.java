package com.freightos.admin.adapter.in.web.menu.dto;

import java.time.LocalDateTime;

public record MenuDetailResponse(
        Long id,
        String menuCode,
        Long parentId,
        String path,
        String label,
        String labelEn,
        String icon,
        Integer sortOrder,
        Boolean active,
        String moduleCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
