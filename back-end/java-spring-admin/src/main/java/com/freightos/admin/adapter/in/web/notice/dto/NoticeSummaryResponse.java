package com.freightos.admin.adapter.in.web.notice.dto;

import java.time.LocalDateTime;

public record NoticeSummaryResponse(
        Long id,
        String title,
        boolean pinned,
        boolean active,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
