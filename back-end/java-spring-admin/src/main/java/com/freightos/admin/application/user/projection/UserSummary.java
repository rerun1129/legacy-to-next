package com.freightos.admin.application.user.projection;

import java.time.LocalDateTime;

public record UserSummary(
        Long id,
        String username,
        String email,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
