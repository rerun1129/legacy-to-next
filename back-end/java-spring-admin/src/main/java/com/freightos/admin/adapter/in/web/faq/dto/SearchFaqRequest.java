package com.freightos.admin.adapter.in.web.faq.dto;

public record SearchFaqRequest(
        Long faqCategoryId,
        String question,
        String scope,
        int page,
        int size
) {}
