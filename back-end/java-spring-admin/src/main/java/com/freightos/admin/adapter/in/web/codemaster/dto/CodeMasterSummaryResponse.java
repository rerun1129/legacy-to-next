package com.freightos.admin.adapter.in.web.codemaster.dto;

import java.time.LocalDateTime;

public record CodeMasterSummaryResponse(
        Long id,
        String masterCode,
        String masterName,
        String description,
        Integer sortOrder,
        boolean active,
        LocalDateTime updatedAt
) {}
