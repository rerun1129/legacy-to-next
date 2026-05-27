package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateExchangeRateItemRequest(
        @NotNull Long id,
        @Size(max = 8) String exchangeDate,
        BigDecimal cashSellExchangeRate,
        BigDecimal cashBuyExchangeRate,
        BigDecimal wireSendExchangeRate,
        BigDecimal wireReceiveExchangeRate,
        BigDecimal standardExchangeRate,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 100) String nameEn,
        @NotNull Boolean active
) {}
