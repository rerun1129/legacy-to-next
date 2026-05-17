package com.freightos.admin.application.faqcategory.command;

public record SearchFaqCategoryCommand(
        String name,
        String scope,
        int page,
        int size
) {}
