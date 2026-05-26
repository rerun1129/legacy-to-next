package com.freightos.admin.adapter.in.web.code.freight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateFreightRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String freightCode,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @Size(max = 500) String description,
        @NotNull Boolean active
) {}
