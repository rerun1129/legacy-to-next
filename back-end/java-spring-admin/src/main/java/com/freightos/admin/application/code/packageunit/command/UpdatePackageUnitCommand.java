package com.freightos.admin.application.code.packageunit.command;

public record UpdatePackageUnitCommand(
        String name,
        String nameEn,
        boolean active
) {}
