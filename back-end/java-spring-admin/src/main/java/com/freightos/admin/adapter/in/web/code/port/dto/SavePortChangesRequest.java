package com.freightos.admin.adapter.in.web.code.port.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavePortChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreatePortRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdatePortItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
