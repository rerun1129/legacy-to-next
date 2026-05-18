package com.freightos.admin.application.user.command;

import com.freightos.admin.application.user.projection.UserScope;

public record SearchUserCommand(
        String username,
        UserScope scope,
        int page,
        int size
) {}
