package com.freightos.admin.application.code.freight.command;

public record CreateFreightCommand(
        String freightCode,
        String name,
        String nameEn,
        String description,
        boolean active
) {}
