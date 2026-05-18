package com.freightos.admin.adapter.in.web.user.dto;

import com.freightos.admin.application.user.projection.UserScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchUserRequest(
        String username,
        UserScope scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
