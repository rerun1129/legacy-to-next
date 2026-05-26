package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchExchangeRateRequest(
        String baseCurrency,
        String targetCurrency,
        String name,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
