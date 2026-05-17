package com.freightos.admin.adapter.in.web.code.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCodeRequest(
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") String codeGroup,
        @NotBlank @Size(max = 40) String codeValue,
        @NotBlank @Size(max = 200) String codeLabel,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active,
        @Size(max = 500) String remark
) {}
