package com.freightos.admin.adapter.in.web.notice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchNoticeRequest(
        String title,
        Boolean pinned,
        String scope,
        Boolean publishedOnly,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
