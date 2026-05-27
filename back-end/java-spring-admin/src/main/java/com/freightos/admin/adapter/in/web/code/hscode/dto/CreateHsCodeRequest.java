package com.freightos.admin.adapter.in.web.code.hscode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHsCodeRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9._-]+$") String hsCode,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @Size(max = 5) String countryCode,
        @NotNull Boolean active
) {}
