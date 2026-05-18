package com.freightos.admin.adapter.in.web.user.dto;

import java.time.LocalDateTime;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
