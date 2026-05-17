package com.freightos.admin.application.faq.projection;

import java.time.LocalDateTime;

public record FaqSummary(
        Long faqId,
        Long faqCategoryId,
        String question,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
