package com.freightos.admin.adapter.in.web.faqcategory.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFaqCategoryRequest(
        @NotBlank String name,
        int sortOrder,
        boolean active
) {}
