package com.freightos.admin.application.code.exchangerate.command;

public record SearchExchangeRateCommand(
        String fromCurrencyCode,
        String toCurrencyCode,
        String name,
        String scope,
        int page,
        int size
) {}
