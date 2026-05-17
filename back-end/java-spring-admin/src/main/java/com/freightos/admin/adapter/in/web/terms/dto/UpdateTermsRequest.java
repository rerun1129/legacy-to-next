package com.freightos.admin.adapter.in.web.terms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateTermsRequest(
        @NotBlank String content,
        String summary,
        @NotNull LocalDateTime effectiveAt
) {}
