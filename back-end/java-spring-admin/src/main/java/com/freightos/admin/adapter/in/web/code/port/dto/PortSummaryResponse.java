package com.freightos.admin.adapter.in.web.code.port.dto;

import com.freightos.admin.domain.code.port.entity.PortType;

import java.time.LocalDateTime;

public record PortSummaryResponse(
        Long id,
        String portCode,
        String name,
        String nameEn,
        String countryCode,
        PortType portType,
        boolean active,
        LocalDateTime deletedAt,
        LocalDateTime updatedAt
) {}
