package com.freightos.admin.adapter.in.web.faqcategory.dto;

public record SearchFaqCategoryRequest(
        String name,
        String scope,
        int page,
        int size
) {}
