package com.freightos.admin.adapter.in.web.codedetail.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCodeDetailItemRequest(
        @NotNull Long id,
        @NotBlank @Size(max = 200) String codeLabel,
        @Min(0) Integer sortOrder,
        @NotNull Boolean active,
        @Size(max = 500) String remark
) {}
