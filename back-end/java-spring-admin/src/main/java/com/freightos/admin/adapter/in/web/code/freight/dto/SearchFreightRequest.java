package com.freightos.admin.adapter.in.web.code.freight.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchFreightRequest(
        String freightCode,
        String name,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
