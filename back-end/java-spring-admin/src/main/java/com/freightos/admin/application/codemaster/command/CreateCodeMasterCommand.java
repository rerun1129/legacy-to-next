package com.freightos.admin.application.codemaster.command;

public record CreateCodeMasterCommand(
        String masterCode,
        String masterName,
        String description,
        Integer sortOrder,
        Boolean active
) {}
