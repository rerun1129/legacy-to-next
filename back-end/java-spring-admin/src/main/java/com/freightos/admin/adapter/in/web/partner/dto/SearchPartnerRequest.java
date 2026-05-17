package com.freightos.admin.adapter.in.web.partner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchPartnerRequest(
        String partnerCode,
        String name,
        String partnerType,
        Boolean active,
        boolean includeDeleted,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
