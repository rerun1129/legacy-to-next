package com.freightos.admin.application.attributedefinition.projection;

import java.time.LocalDateTime;

public record AttributeDefinitionSummary(
        Long id,
        String attributeKey,
        String name,
        String description,
        String valueType,
        Boolean active,
        LocalDateTime updatedAt
) {}
