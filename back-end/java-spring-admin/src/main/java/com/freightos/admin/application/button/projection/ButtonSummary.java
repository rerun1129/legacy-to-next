package com.freightos.admin.application.button.projection;

import java.time.LocalDateTime;

public record ButtonSummary(
        Long id,
        String buttonCode,
        Long menuId,
        String label,
        String actionType,
        String apiMethod,
        String apiPath,
        Integer sortOrder,
        Boolean active,
        LocalDateTime updatedAt
) {}
