package com.freightos.admin.adapter.in.web.attributevalue.dto;

import java.time.LocalDateTime;

public record AttributeValueDetailResponse(
        String attributeKey,
        String value,
        String label,
        Integer sortOrder,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
