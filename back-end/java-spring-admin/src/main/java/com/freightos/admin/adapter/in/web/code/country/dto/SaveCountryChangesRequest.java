package com.freightos.admin.adapter.in.web.code.country.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCountryChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateCountryRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCountryItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
