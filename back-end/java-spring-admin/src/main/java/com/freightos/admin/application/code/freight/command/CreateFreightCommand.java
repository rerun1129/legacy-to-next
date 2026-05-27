package com.freightos.admin.application.code.freight.command;

public record CreateFreightCommand(
        String freightCode,
        String name,
        String nameEn,
        String description,
        String freightUnit,
        String freightGroup,
        boolean active
) {}
