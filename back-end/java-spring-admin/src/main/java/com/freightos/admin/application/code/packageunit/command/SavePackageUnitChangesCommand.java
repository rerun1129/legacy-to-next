package com.freightos.admin.application.code.packageunit.command;

import java.util.List;

public record SavePackageUnitChangesCommand(
        List<CreatePackageUnitCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdatePackageUnitCommand command) {}
}
