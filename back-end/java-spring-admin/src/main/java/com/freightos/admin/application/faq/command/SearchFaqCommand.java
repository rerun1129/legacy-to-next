package com.freightos.admin.application.faq.command;

public record SearchFaqCommand(
        Long faqCategoryId,
        String question,
        String scope,
        int page,
        int size
) {}
