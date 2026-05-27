package com.freightos.admin.adapter.in.web.code.freight.dto;

import com.freightos.admin.domain.code.freight.FreightGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFreightRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @Size(max = 500) String description,
        @Size(max = 10) String freightUnit,
        FreightGroup freightGroup,
        @NotNull Boolean active
) {}
