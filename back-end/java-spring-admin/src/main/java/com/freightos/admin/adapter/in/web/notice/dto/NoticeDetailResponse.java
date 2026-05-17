package com.freightos.admin.adapter.in.web.notice.dto;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long id,
        String title,
        String content,
        boolean pinned,
        boolean active,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
