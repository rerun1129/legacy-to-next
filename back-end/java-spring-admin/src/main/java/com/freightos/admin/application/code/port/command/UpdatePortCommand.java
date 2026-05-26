package com.freightos.admin.application.code.port.command;

import com.freightos.admin.domain.code.port.entity.PortType;

public record UpdatePortCommand(
        String name,
        String nameEn,
        String countryCode,
        PortType portType,
        boolean active
) {}
