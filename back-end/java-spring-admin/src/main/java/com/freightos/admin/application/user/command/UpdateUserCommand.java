package com.freightos.admin.application.user.command;

import com.freightos.admin.domain.user.entity.UserRole;

public record UpdateUserCommand(
        String email,
        String rawPasswordOrNull,
        UserRole role,
        boolean active
) {}
