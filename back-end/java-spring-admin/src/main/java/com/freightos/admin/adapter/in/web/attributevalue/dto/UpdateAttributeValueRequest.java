package com.freightos.admin.adapter.in.web.attributevalue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAttributeValueRequest(
        @NotBlank @Size(max = 200) String label,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active
) {}
