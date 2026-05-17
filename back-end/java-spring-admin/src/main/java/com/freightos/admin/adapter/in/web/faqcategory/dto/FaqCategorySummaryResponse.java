package com.freightos.admin.adapter.in.web.faqcategory.dto;

import java.time.LocalDateTime;

public record FaqCategorySummaryResponse(
        Long faqCategoryId,
        String name,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
