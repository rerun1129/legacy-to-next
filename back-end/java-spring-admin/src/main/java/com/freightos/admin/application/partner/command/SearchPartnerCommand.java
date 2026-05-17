package com.freightos.admin.application.partner.command;

public record SearchPartnerCommand(
        String partnerCode,
        String name,
        String partnerType,
        Boolean active,
        boolean includeDeleted,
        int page,
        int size
) {}
