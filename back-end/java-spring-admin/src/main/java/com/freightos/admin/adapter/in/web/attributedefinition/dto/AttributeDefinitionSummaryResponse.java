package com.freightos.admin.adapter.in.web.attributedefinition.dto;

import java.time.LocalDateTime;

public record AttributeDefinitionSummaryResponse(
        Long id,
        String attributeKey,
        String name,
        String description,
        String valueType,
        Boolean active,
        Boolean allowMulti,
        LocalDateTime updatedAt
) {}
