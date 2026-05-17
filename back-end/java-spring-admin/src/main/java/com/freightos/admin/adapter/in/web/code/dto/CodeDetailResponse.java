package com.freightos.admin.adapter.in.web.code.dto;

import java.time.LocalDateTime;

public record CodeDetailResponse(
        Long id,
        String codeGroup,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
