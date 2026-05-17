package com.freightos.admin.application.faqcategory.projection;

import java.time.LocalDateTime;

public record FaqCategorySummary(
        Long faqCategoryId,
        String name,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
