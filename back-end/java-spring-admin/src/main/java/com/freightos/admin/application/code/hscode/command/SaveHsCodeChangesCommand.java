package com.freightos.admin.application.code.hscode.command;

import java.util.List;

public record SaveHsCodeChangesCommand(
        List<CreateHsCodeCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateHsCodeCommand command) {}
}
