package com.freightos.admin.application.code.exchangerate.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateSummary(
        Long id,
        String fromCurrencyCode,
        String toCurrencyCode,
        String exchangeDate,
        BigDecimal cashSellExchangeRate,
        BigDecimal cashBuyExchangeRate,
        BigDecimal wireSendExchangeRate,
        BigDecimal wireReceiveExchangeRate,
        BigDecimal standardExchangeRate,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
