package com.freightos.admin.adapter.in.web.buttonpolicy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateButtonPolicyRequest(
        @NotNull Long buttonId,
        @NotBlank @Size(max = 80) String attributeKey,
        @NotBlank @Size(max = 100) String requiredValue
) {}
