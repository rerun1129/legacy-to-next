package com.freightos.admin.application.notice.projection;

import java.time.LocalDateTime;

public record NoticeSummary(
        Long id,
        String title,
        boolean pinned,
        boolean active,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
