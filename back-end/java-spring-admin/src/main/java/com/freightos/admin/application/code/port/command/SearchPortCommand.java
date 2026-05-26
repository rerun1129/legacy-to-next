package com.freightos.admin.application.code.port.command;

public record SearchPortCommand(
        String portCode,
        String name,
        String countryCode,
        String portType,
        String scope,
        int page,
        int size
) {}
