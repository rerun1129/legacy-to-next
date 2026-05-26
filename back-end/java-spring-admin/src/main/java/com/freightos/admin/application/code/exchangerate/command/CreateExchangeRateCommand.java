package com.freightos.admin.application.code.exchangerate.command;

import java.math.BigDecimal;

public record CreateExchangeRateCommand(
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        String name,
        String nameEn,
        boolean active
) {}
