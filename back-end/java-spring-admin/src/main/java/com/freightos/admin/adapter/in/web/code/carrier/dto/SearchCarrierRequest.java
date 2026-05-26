package com.freightos.admin.adapter.in.web.code.carrier.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchCarrierRequest(
        String carrierCode,
        String name,
        String carrierType,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
