package com.freightos.admin.application.faqcategory.command;

public record CreateFaqCategoryCommand(
        String name,
        int sortOrder,
        boolean active
) {}
