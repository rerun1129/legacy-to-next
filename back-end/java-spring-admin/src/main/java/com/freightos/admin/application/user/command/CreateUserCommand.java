package com.freightos.admin.application.user.command;

import com.freightos.admin.domain.user.entity.UserRole;

public record CreateUserCommand(
        String username,
        String email,
        String rawPassword,
        UserRole role,
        boolean active
) {}
