package com.freightos.admin.application.code.command;

public record SearchCodeCommand(
        String codeGroup,
        String codeValue,
        String codeLabel,
        Boolean active,
        int page,
        int size
) {}
