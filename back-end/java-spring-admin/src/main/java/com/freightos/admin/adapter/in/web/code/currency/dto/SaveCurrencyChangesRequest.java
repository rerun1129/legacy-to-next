package com.freightos.admin.adapter.in.web.code.currency.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCurrencyChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateCurrencyRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCurrencyItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
