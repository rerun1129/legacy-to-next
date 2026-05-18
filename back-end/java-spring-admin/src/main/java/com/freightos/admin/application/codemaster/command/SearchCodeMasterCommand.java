package com.freightos.admin.application.codemaster.command;

public record SearchCodeMasterCommand(
        String masterCode,
        String masterName,
        Boolean active,
        int page,
        int size
) {}
