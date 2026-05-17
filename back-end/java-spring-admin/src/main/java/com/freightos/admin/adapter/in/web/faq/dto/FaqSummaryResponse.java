package com.freightos.admin.adapter.in.web.faq.dto;

import java.time.LocalDateTime;

public record FaqSummaryResponse(
        Long faqId,
        Long faqCategoryId,
        String question,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
