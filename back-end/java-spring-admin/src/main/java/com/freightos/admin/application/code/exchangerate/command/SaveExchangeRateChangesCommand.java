package com.freightos.admin.application.code.exchangerate.command;

import java.util.List;

public record SaveExchangeRateChangesCommand(
        List<CreateExchangeRateCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateExchangeRateCommand command) {}
}
