package com.freightos.admin.application.code.currency.command;

import java.util.List;

public record SaveCurrencyChangesCommand(
        List<CreateCurrencyCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCurrencyCommand command) {}
}
