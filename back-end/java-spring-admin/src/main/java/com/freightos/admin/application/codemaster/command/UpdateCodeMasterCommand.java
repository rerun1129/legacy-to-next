package com.freightos.admin.application.codemaster.command;

public record UpdateCodeMasterCommand(
        String masterName,
        String description,
        Integer sortOrder,
        Boolean active
) {}
