package com.freightos.admin.application.code.freight.command;

public record UpdateFreightCommand(
        String name,
        String nameEn,
        String description,
        String freightUnit,
        String freightGroup,
        boolean active
) {}
