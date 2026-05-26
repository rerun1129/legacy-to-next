package com.freightos.admin.application.code.hscode.projection;

import java.time.LocalDateTime;

public record HsCodeSummary(
        Long id,
        String hsCode,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
