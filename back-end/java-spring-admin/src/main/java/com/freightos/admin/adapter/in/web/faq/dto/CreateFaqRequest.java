package com.freightos.admin.adapter.in.web.faq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFaqRequest(
        @NotNull Long faqCategoryId,
        @NotBlank String question,
        @NotBlank String answer,
        int sortOrder,
        boolean active
) {}
