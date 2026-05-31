package com.freightos.admin.application.user.command;

import java.util.List;
import java.util.Map;

public record UpdateUserCommand(
        String email,
        String rawPasswordOrNull,
        boolean active,
        Map<String, List<String>> attributes,
        Long teamId,
        Long subscriberId
) {}
