package com.freightos.admin.application.code.port.projection;

import com.freightos.admin.domain.code.port.entity.PortType;

import java.time.LocalDateTime;

public record PortSummary(
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
