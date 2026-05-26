package com.freightos.admin.application.code.freight.command;

public record SearchFreightCommand(
        String freightCode,
        String name,
        String scope,
        int page,
        int size
) {}
