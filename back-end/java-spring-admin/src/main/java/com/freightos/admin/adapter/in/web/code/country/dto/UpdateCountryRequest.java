package com.freightos.admin.adapter.in.web.code.country.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCountryRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @NotNull Boolean active
) {}
