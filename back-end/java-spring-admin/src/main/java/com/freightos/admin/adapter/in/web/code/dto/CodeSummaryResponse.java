package com.freightos.admin.adapter.in.web.code.dto;

import java.time.LocalDateTime;

public record CodeSummaryResponse(
        Long id,
        String codeGroup,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        LocalDateTime updatedAt
) {}
