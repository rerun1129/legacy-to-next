package com.freightos.admin.application.code.exchangerate.command;

import java.math.BigDecimal;

public record UpdateExchangeRateCommand(
        BigDecimal cashSellExchangeRate,
        BigDecimal cashBuyExchangeRate,
        BigDecimal wireSendExchangeRate,
        BigDecimal wireReceiveExchangeRate,
        BigDecimal standardExchangeRate,
        String name,
        String nameEn,
        boolean active
) {}
