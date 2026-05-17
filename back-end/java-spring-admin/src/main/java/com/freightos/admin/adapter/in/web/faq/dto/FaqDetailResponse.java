package com.freightos.admin.adapter.in.web.faq.dto;

import java.time.LocalDateTime;

public record FaqDetailResponse(
        Long faqId,
        Long faqCategoryId,
        String question,
        String answer,
        int sortOrder,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
