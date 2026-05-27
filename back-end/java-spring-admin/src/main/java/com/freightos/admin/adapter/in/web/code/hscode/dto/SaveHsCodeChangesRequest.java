package com.freightos.admin.adapter.in.web.code.hscode.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveHsCodeChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateHsCodeRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateHsCodeItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
