package com.freightos.admin.adapter.in.web.user.dto;

import com.freightos.admin.domain.user.entity.UserRole;
import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
