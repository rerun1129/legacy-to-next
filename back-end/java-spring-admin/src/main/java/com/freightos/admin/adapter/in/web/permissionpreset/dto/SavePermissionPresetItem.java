package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** bulk save-changes 요청 중 update 항목. code 는 식별자이므로 의도적으로 제외한다. */
public record SavePermissionPresetItem(
        @NotNull Long id,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 512) String description,
        @NotNull Boolean active
) {}
