package com.freightos.admin.adapter.in.web.code.port.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchPortRequest(
        String portCode,
        String name,
        String countryCode,
        String portType,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
