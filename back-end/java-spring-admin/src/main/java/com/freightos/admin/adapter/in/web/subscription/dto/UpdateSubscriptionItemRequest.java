package com.freightos.admin.adapter.in.web.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateSubscriptionItemRequest(
        @NotNull Long id,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Boolean active
) {}
