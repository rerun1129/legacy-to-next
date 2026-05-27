package com.freightos.admin.application.code.exchangerate.projection;

import java.time.LocalDateTime;

public record ExchangeRateSummary(
        Long id,
        String fromCurrencyCode,
        String toCurrencyCode,
        String exchangeDate,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
