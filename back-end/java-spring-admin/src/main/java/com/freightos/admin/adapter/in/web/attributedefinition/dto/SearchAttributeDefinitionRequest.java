package com.freightos.admin.adapter.in.web.attributedefinition.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchAttributeDefinitionRequest(
        String attributeKey,
        String name,
        String valueType,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
