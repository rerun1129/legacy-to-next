package com.freightos.admin.adapter.in.web.faqcategory.dto;

import java.time.LocalDateTime;

public record FaqCategoryDetailResponse(
        Long faqCategoryId,
        String name,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
