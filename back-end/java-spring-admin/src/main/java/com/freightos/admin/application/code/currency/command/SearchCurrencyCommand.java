package com.freightos.admin.application.code.currency.command;

public record SearchCurrencyCommand(
        String currencyCode,
        String name,
        String scope,
        int page,
        int size
) {}
