package com.freightos.admin.adapter.in.web.attributevalue.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchAttributeValueRequest(
        String attributeKey,
        String value,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
