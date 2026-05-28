package com.freightos.admin.adapter.in.web.userpermissionpreset.dto;

import jakarta.validation.constraints.NotNull;

public record AssignUserPermissionPresetRequest(
        @NotNull Long userId,
        @NotNull Long presetId
) {}
