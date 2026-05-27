package com.freightos.admin.adapter.in.web.code.packageunit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavePackageUnitChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreatePackageUnitRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdatePackageUnitItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
