package com.freightos.admin.application.code.hscode.command;

public record SearchHsCodeCommand(
        String hsCode,
        String name,
        String scope,
        int page,
        int size
) {}
