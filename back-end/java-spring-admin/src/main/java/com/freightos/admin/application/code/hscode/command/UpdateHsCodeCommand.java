package com.freightos.admin.application.code.hscode.command;

public record UpdateHsCodeCommand(
        String name,
        String nameEn,
        String countryCode,
        boolean active
) {}
