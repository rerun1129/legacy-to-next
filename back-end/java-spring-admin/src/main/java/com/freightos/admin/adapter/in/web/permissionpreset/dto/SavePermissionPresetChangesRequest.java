package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavePermissionPresetChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreatePermissionPresetRequest> creates,
        @Valid @Size(max = 50) List<@Valid SavePermissionPresetItem> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
