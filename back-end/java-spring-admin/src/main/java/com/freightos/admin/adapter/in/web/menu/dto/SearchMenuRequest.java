package com.freightos.admin.adapter.in.web.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchMenuRequest(
        String menuCode,
        String label,
        String moduleCode,
        Long parentId,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
