package com.freightos.admin.application.terms.projection;

import java.time.LocalDateTime;

public record TermsSummary(
        Long termsId,
        String type,
        int version,
        LocalDateTime effectiveAt,
        String summary,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
