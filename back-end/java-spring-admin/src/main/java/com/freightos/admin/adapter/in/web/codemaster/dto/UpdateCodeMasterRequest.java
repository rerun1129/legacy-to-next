package com.freightos.admin.adapter.in.web.codemaster.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCodeMasterRequest(
        @NotBlank @Size(max = 200) String masterName,
        @Size(max = 500) String description,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active
) {}
