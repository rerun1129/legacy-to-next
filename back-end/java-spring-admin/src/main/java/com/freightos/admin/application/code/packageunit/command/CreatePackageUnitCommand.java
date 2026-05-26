package com.freightos.admin.application.code.packageunit.command;

public record CreatePackageUnitCommand(
        String packageCode,
        String name,
        String nameEn,
        boolean active
) {}
