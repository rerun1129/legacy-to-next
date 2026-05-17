package com.freightos.admin.adapter.in.web.user.dto;

import com.freightos.admin.domain.user.entity.UserRole;

import java.time.LocalDateTime;

/** passwordHash는 절대 포함하지 않는다. */
public record UserDetailResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
