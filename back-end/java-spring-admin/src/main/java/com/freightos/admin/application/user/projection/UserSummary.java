package com.freightos.admin.application.user.projection;

import com.freightos.admin.domain.user.entity.UserRole;
import java.time.LocalDateTime;

public record UserSummary(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
