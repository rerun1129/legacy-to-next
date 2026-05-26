package com.freightos.admin.application.code.hscode.command;

public record CreateHsCodeCommand(
        String hsCode,
        String name,
        String nameEn,
        boolean active
) {}
