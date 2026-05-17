package com.freightos.admin.adapter.in.web.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateNoticeRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank String content,
        @NotNull Boolean pinned,
        @NotNull Boolean active,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt
) {}
