package com.freightos.admin.adapter.in.web.code.freight.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveFreightChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateFreightRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateFreightItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
