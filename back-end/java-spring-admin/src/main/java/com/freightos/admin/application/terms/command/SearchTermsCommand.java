package com.freightos.admin.application.terms.command;

public record SearchTermsCommand(
        String type,
        String scope,
        Integer version,
        String summary,
        int page,
        int size
) {}
