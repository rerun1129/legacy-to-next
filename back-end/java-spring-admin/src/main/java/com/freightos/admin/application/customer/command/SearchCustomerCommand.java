package com.freightos.admin.application.customer.command;

public record SearchCustomerCommand(
        String customerCode,
        String name,
        String customerType,
        Boolean active,
        boolean includeDeleted,
        int page,
        int size
) {}
