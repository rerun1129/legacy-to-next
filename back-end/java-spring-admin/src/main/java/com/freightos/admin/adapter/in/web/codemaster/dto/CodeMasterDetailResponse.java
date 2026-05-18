package com.freightos.admin.adapter.in.web.codemaster.dto;

import java.time.LocalDateTime;

public record CodeMasterDetailResponse(
        Long id,
        String masterCode,
        String masterName,
        String description,
        Integer sortOrder,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
