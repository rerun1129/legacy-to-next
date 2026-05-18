package com.freightos.admin.adapter.in.web.attributedefinition.dto;

import java.time.LocalDateTime;

public record AttributeDefinitionDetailResponse(
        Long id,
        String attributeKey,
        String name,
        String description,
        String valueType,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
