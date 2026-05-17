package com.freightos.admin.application.user.command;

import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;

import java.util.Set;

public record UpdateUserCommand(
        String email,
        String rawPasswordOrNull,
        UserRole role,
        boolean active,
        Set<Permission> permissions
) {}
