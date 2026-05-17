package com.freightos.admin.application.user.command;

import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;

import java.util.Set;

public record CreateUserCommand(
        String username,
        String email,
        String rawPassword,
        UserRole role,
        boolean active,
        Set<Permission> permissions
) {}
