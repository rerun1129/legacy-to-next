package com.freightos.admin.application.notice.command;

import java.time.LocalDateTime;

public record CreateNoticeCommand(
        String title,
        String content,
        boolean pinned,
        boolean active,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt
) {}
