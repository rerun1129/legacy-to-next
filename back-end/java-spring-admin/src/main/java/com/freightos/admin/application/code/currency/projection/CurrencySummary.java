package com.freightos.admin.application.code.currency.projection;

import java.time.LocalDateTime;

public record CurrencySummary(
        Long id,
        String currencyCode,
        String name,
        String symbol,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
