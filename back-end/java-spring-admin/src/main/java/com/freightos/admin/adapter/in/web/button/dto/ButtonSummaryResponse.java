package com.freightos.admin.adapter.in.web.button.dto;

import java.time.LocalDateTime;

public record ButtonSummaryResponse(
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
