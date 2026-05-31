package com.freightos.admin.adapter.in.web.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateSubscriptionItemRequest(
        @NotBlank @Size(max = 40) String moduleCode,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull Boolean active
) {}
