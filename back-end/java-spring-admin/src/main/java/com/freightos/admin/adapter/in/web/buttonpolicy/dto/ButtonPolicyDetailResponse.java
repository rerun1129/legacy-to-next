package com.freightos.admin.adapter.in.web.buttonpolicy.dto;

import java.time.LocalDateTime;

public record ButtonPolicyDetailResponse(
        Long id,
        Long buttonId,
        String attributeKey,
        String requiredValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
