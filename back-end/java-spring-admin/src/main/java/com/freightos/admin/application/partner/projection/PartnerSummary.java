package com.freightos.admin.application.partner.projection;

import com.freightos.admin.domain.partner.entity.PartnerType;

import java.time.LocalDateTime;

public record PartnerSummary(
        Long id,
        String partnerCode,
        PartnerType partnerType,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
