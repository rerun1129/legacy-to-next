package com.freightos.admin.adapter.in.web.module.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateModuleRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String description,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active
) {}
