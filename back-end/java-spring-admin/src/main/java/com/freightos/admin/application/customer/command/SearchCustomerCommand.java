package com.freightos.admin.application.customer.command;

public record SearchCustomerCommand(
        String customerCode,
        String name,
        String customerType,
        String scope,
        int page,
        int size
) {}
