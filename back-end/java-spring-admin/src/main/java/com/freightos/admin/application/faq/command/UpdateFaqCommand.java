package com.freightos.admin.application.faq.command;

public record UpdateFaqCommand(
        Long faqCategoryId,
        String question,
        String answer,
        int sortOrder,
        boolean active
) {}
