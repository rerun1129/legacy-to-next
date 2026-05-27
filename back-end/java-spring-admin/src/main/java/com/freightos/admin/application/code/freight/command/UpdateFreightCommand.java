package com.freightos.admin.application.code.freight.command;

import com.freightos.admin.domain.code.freight.FreightGroup;

public record UpdateFreightCommand(
        String name,
        String nameEn,
        String description,
        String freightUnit,
        FreightGroup freightGroup,
        boolean active
) {}
