package com.freightos.admin.adapter.in.web.menupolicy.dto;

import java.time.LocalDateTime;

public record MenuPolicyDetailResponse(
        Long id,
        Long menuId,
        String attributeKey,
        String requiredValue,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {}
