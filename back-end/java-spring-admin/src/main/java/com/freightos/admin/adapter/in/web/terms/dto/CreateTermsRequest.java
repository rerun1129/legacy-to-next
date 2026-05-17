package com.freightos.admin.adapter.in.web.terms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTermsRequest(
        @NotBlank String type,
        @Min(1) int version,
        @NotNull LocalDateTime effectiveAt,
        @NotBlank String content,
        String summary
) {}
