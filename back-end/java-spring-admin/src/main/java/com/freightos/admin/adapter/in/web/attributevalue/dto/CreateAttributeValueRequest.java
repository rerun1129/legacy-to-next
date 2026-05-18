package com.freightos.admin.adapter.in.web.attributevalue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAttributeValueRequest(
        @NotBlank @Size(max = 80) String attributeKey,
        @NotBlank @Size(max = 100) String value,
        @NotBlank @Size(max = 200) String label,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active
) {}
