package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExchangeRateDetailResponse(
        Long id,
        String baseCurrency,
        String targetCurrency,
        BigDecimal rate,
        String name,
        String nameEn,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
