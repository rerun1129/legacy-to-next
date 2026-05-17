package com.freightos.admin.application.user.command;

import com.freightos.admin.application.user.projection.UserScope;
import com.freightos.admin.domain.user.entity.UserRole;

public record SearchUserCommand(
        String username,
        UserRole role,
        UserScope scope,
        int page,
        int size
) {}
