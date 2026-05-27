package com.freightos.admin.application.code.port.command;

import java.util.List;

public record SavePortChangesCommand(
        List<CreatePortCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdatePortCommand command) {}
}
