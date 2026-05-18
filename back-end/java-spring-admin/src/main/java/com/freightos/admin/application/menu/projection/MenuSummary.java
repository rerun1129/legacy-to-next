package com.freightos.admin.application.menu.projection;

import java.time.LocalDateTime;

public record MenuSummary(
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
        LocalDateTime updatedAt
) {}
