package com.freightos.admin.application.customer.command;

import java.util.List;

public record SaveCustomerChangesCommand(
        List<CreateCustomerCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateCustomerCommand command) {}
}
