package com.freightos.admin.adapter.in.web.code.freight.dto;

import com.freightos.admin.domain.code.freight.FreightGroup;

import java.time.LocalDateTime;

public record FreightSummaryResponse(
        Long id,
        String freightCode,
        String name,
        String nameEn,
        String description,
        String freightUnit,
        FreightGroup freightGroup,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
