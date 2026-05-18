package com.freightos.admin.application.attributevalue.projection;

import java.time.LocalDateTime;

public record AttributeValueSummary(
        String attributeKey,
        String value,
        String label,
        Integer sortOrder,
        Boolean active,
        LocalDateTime updatedAt
) {}
