package com.freightos.admin.application.subscriber.command;

import java.util.List;

public record SaveSubscriberChangesCommand(
        List<CreateSubscriberCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateSubscriberCommand command) {}
}
