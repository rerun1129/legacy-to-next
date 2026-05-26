package com.freightos.admin.application.code.exchangerate.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateSummary(
        Long id,
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
