package com.freightos.admin.application.codemaster.command;

import java.util.List;

public record SaveCodeMasterChangesCommand(
        List<CreateCodeMasterCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCodeMasterCommand command) {}
}
