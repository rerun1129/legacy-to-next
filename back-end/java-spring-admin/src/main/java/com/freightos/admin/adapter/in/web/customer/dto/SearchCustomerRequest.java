package com.freightos.admin.adapter.in.web.customer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchCustomerRequest(
        String customerCode,
        String name,
        String customerType,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
