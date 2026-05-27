package com.freightos.admin.adapter.in.web.code.carrier.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCarrierChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateCarrierRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCarrierItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
