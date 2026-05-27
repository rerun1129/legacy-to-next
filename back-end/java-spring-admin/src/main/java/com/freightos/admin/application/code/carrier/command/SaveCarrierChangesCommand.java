package com.freightos.admin.application.code.carrier.command;

import java.util.List;

public record SaveCarrierChangesCommand(
        List<CreateCarrierCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCarrierCommand command) {}
}
