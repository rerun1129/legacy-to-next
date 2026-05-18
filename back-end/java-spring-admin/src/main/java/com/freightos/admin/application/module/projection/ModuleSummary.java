package com.freightos.admin.application.module.projection;

import java.time.LocalDateTime;

public record ModuleSummary(
        Long id,
        String moduleCode,
        String name,
        String description,
        Integer sortOrder,
        Boolean active,
        LocalDateTime updatedAt
) {}
