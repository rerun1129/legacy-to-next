package com.freightos.admin.adapter.in.web.codedetail.dto;

import java.time.LocalDateTime;

public record CodeDetailDetailResponse(
        Long id,
        Long masterId,
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
