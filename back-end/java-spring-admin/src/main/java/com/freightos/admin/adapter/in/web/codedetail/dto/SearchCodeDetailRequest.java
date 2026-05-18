package com.freightos.admin.adapter.in.web.codedetail.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchCodeDetailRequest(
        Long masterId,
        String codeValue,
        String codeLabel,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
