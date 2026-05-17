package com.freightos.admin.application.code.projection;

import java.time.LocalDateTime;

public record CodeSummary(
        Long id,
        String codeGroup,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        LocalDateTime updatedAt
) {}
