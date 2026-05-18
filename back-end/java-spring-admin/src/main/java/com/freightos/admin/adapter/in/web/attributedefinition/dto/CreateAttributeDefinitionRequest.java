package com.freightos.admin.adapter.in.web.attributedefinition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAttributeDefinitionRequest(
        @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z][a-z0-9_]*$") String attributeKey,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String description,
        @NotBlank String valueType,
        @NotNull Boolean active,
        @NotNull Boolean allowMulti
) {}
