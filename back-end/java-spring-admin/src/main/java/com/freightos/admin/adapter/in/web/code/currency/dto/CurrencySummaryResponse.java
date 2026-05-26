package com.freightos.admin.adapter.in.web.code.currency.dto;

import java.time.LocalDateTime;

public record CurrencySummaryResponse(
        Long id,
        String currencyCode,
        String name,
        String symbol,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
