package com.freightos.admin.application.code.exchangerate.command;

public record SearchExchangeRateCommand(
        String baseCurrency,
        String targetCurrency,
        String name,
        String scope,
        int page,
        int size
) {}
