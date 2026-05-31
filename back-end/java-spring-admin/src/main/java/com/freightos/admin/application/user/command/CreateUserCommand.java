package com.freightos.admin.application.user.command;

import java.util.List;
import java.util.Map;

public record CreateUserCommand(
        String username,
        String email,
        String rawPassword,
        boolean active,
        Map<String, List<String>> attributes,
        Long teamId,
        Long subscriberId
) {}
