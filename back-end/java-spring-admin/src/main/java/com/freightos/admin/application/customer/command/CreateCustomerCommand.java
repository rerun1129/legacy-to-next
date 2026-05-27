package com.freightos.admin.application.customer.command;

import com.freightos.admin.domain.customer.entity.CustomerType;

public record CreateCustomerCommand(
        String customerCode,
        CustomerType customerType,
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String customerLocalAddress,
        String customerEnglishAddress,
        String countryCode,
        String memo,
        boolean active
) {}
