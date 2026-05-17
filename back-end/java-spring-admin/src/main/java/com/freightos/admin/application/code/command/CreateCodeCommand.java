package com.freightos.admin.application.code.command;

public record CreateCodeCommand(
        String codeGroup,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        boolean active,
        String remark
) {}
