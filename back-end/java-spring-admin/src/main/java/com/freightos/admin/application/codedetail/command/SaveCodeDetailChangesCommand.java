package com.freightos.admin.application.codedetail.command;

import java.util.List;

public record SaveCodeDetailChangesCommand(
        Long masterId,
        List<CreateCodeDetailCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCodeDetailCommand command) {}
}
