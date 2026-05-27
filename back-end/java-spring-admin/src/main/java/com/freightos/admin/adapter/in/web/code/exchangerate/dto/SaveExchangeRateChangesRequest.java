package com.freightos.admin.adapter.in.web.code.exchangerate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveExchangeRateChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateExchangeRateRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateExchangeRateItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
