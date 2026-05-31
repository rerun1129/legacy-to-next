package com.freightos.admin.application.user.projection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record UserSummary(
        Long id,
        String username,
        String email,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt,
        Map<String, List<String>> attributes,
        Long teamId,
        Long subscriberId
) {}
