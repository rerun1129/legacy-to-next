package com.freightos.admin.application.code.command;

public record UpdateCodeCommand(
        String codeLabel,
        Integer sortOrder,
        boolean active,
        String remark
) {}
