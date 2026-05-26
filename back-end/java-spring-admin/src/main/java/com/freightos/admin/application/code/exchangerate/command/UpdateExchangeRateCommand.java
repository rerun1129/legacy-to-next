package com.freightos.admin.application.code.exchangerate.command;

import java.math.BigDecimal;

public record UpdateExchangeRateCommand(
        BigDecimal rate,
        String name,
        String nameEn,
        boolean active
) {}
