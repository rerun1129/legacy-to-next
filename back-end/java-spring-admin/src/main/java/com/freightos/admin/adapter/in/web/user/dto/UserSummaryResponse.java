package com.freightos.admin.adapter.in.web.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt,
        Map<String, List<String>> attributes,
        Long teamId
) {}
