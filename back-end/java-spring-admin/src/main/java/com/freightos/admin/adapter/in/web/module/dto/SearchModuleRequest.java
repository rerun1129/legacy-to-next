package com.freightos.admin.adapter.in.web.module.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchModuleRequest(
        String moduleCode,
        String name,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
