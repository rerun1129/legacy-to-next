package com.freightos.admin.adapter.in.web.codemaster.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchCodeMasterRequest(
        String masterCode,
        String masterName,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
