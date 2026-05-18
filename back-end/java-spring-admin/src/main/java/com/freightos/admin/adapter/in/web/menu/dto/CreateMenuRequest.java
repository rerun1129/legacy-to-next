package com.freightos.admin.adapter.in.web.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(
        @NotBlank @Size(max = 80) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") String menuCode,
        Long parentId,
        @Size(max = 200) String path,
        @NotBlank @Size(max = 200) String label,
        @Size(max = 200) String labelEn,
        @Size(max = 100) String icon,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active,
        @NotBlank @Size(max = 40) String moduleCode
) {}
