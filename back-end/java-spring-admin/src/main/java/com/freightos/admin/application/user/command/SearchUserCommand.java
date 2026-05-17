package com.freightos.admin.application.user.command;

import com.freightos.admin.domain.user.entity.UserRole;

public record SearchUserCommand(
        String username,
        UserRole role,
        Boolean active,
        int page,
        int size
) {}
