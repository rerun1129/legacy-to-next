package com.freightos.admin.application.code.hscode.command;

public record UpdateHsCodeCommand(
        String name,
        String nameEn,
        boolean active
) {}
