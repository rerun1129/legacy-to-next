package com.freightos.admin.application.codedetail.projection;

import java.time.LocalDateTime;

public record CodeDetailSummary(
        Long id,
        Long masterId,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        String remark,
        LocalDateTime updatedAt
) {}
