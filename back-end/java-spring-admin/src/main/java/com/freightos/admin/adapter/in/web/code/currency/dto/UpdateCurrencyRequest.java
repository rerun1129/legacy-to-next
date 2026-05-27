package com.freightos.admin.adapter.in.web.code.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCurrencyRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @Size(max = 10) String symbol,
        Integer currencyUnit,
        @NotNull Boolean active
) {}
