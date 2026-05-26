package com.freightos.admin.application.code.currency.command;

public record CreateCurrencyCommand(
        String currencyCode,
        String name,
        String nameEn,
        String symbol,
        boolean active
) {}
