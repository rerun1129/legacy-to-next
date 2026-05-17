package com.freightos.admin.adapter.in.web.terms.dto;

import java.time.LocalDateTime;

public record TermsDetailResponse(
        Long termsId,
        String type,
        int version,
        LocalDateTime effectiveAt,
        String content,
        String summary,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
