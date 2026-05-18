package com.freightos.admin.application.codemaster.projection;

import java.time.LocalDateTime;

public record CodeMasterSummary(
        Long id,
        String masterCode,
        String masterName,
        String description,
        Integer sortOrder,
        boolean active,
        LocalDateTime updatedAt
) {}
