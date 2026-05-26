package com.freightos.admin.application.code.currency.command;

public record UpdateCurrencyCommand(
        String name,
        String nameEn,
        String symbol,
        boolean active
) {}
