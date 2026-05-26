package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateSummaryResponse(
        Long id,
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
