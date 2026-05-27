package com.freightos.admin.application.code.country.command;

import java.util.List;

public record SaveCountryChangesCommand(
        List<CreateCountryCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCountryCommand command) {}
}
