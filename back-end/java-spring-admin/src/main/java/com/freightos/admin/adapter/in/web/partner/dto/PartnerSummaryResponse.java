package com.freightos.admin.adapter.in.web.partner.dto;

import com.freightos.admin.domain.partner.entity.PartnerType;

import java.time.LocalDateTime;

public record PartnerSummaryResponse(
        Long id,
        String partnerCode,
        PartnerType partnerType,
        String name,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
