package com.freightos.admin.adapter.in.web.code.carrier.dto;

import com.freightos.admin.domain.code.carrier.entity.CarrierType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCarrierRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String carrierCode,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @NotNull CarrierType carrierType,
        @Size(max = 4000) String carrierAddress,
        @Size(max = 2) String ediCode,
        @NotNull Boolean active
) {}
