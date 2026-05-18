package com.freightos.admin.adapter.in.web.button.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchButtonRequest(
        Long menuId,
        String buttonCode,
        String label,
        String actionType,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
