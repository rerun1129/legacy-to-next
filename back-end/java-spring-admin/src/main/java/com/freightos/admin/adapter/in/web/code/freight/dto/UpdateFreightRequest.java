package com.freightos.admin.adapter.in.web.code.freight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFreightRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @Size(max = 500) String description,
        @Size(max = 10) String freightUnit,
        @Size(max = 50) String freightGroup,
        @NotNull Boolean active
) {}
