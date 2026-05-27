package com.freightos.admin.adapter.in.web.code.currency.dto;

import java.time.LocalDateTime;

public record CurrencyDetailResponse(
        Long id,
        String currencyCode,
        String name,
        String nameEn,
        String symbol,
        Integer currencyUnit,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
