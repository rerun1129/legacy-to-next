package com.freightos.admin.adapter.in.web.codedetail.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCodeDetailChangesRequest(
        @NotNull @Positive Long masterId,
        @Valid @Size(max = 50) List<@Valid CreateCodeDetailRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCodeDetailItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
