package com.freightos.admin.adapter.in.web.attributevalue.dto;

import java.time.LocalDateTime;

public record AttributeValueSummaryResponse(
        Long id,
        String attributeKey,
        String value,
        String label,
        Integer sortOrder,
        Boolean active,
        LocalDateTime updatedAt
) {}
