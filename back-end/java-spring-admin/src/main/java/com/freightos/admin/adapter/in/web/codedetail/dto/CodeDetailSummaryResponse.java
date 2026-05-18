package com.freightos.admin.adapter.in.web.codedetail.dto;

import java.time.LocalDateTime;

public record CodeDetailSummaryResponse(
        Long id,
        Long masterId,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        LocalDateTime updatedAt
) {}
