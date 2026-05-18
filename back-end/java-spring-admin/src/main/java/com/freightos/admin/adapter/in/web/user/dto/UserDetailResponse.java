package com.freightos.admin.adapter.in.web.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** passwordHash는 절대 포함하지 않는다. */
public record UserDetailResponse(
        Long id,
        String username,
        String email,
        boolean active,
        LocalDateTime deletedAt,
        Map<String, List<String>> attributes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
