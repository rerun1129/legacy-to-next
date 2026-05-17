package com.freightos.admin.adapter.in.web.terms.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchTermsRequest(
        String type,
        String scope,
        Integer version,
        String summary,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
