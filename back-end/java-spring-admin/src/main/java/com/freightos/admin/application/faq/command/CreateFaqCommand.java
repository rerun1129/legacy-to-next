package com.freightos.admin.application.faq.command;

public record CreateFaqCommand(
        Long faqCategoryId,
        String question,
        String answer,
        int sortOrder,
        boolean active
) {}
