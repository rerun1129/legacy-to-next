package com.freightos.admin.application.code.packageunit.command;

public record SearchPackageUnitCommand(
        String packageCode,
        String name,
        String scope,
        int page,
        int size
) {}
