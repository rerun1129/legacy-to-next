package com.freightos.admin.adapter.in.web.terms.dto;

import java.time.LocalDateTime;

public record TermsSummaryResponse(
        Long termsId,
        String type,
        int version,
        LocalDateTime effectiveAt,
        String summary,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
