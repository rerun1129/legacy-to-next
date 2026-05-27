package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import java.time.LocalDateTime;

public record ExchangeRateSummaryResponse(
        Long id,
        String fromCurrencyCode,
        String toCurrencyCode,
        String exchangeDate,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
