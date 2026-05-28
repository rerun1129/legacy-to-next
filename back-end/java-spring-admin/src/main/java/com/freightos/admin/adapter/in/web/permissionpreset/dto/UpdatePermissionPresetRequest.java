package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePermissionPresetRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 512) String description,
        @NotNull Boolean active
) {}
