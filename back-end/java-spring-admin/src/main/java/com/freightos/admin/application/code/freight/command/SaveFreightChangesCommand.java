package com.freightos.admin.application.code.freight.command;

import java.util.List;

public record SaveFreightChangesCommand(
        List<CreateFreightCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateFreightCommand command) {}
}
