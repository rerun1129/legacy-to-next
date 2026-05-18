package com.freightos.admin.adapter.in.web.menupolicy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchMenuPolicyRequest(
        Long menuId,
        String attributeKey,
        String requiredValue,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
