package com.freightos.admin.application.faqcategory.command;

public record UpdateFaqCategoryCommand(
        String name,
        int sortOrder,
        boolean active
) {}
