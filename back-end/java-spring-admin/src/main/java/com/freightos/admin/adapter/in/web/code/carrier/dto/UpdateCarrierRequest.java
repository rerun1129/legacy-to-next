package com.freightos.admin.adapter.in.web.code.carrier.dto;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCarrierRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @NotNull CarrierType carrierType,
        @NotNull Boolean active
) {}
