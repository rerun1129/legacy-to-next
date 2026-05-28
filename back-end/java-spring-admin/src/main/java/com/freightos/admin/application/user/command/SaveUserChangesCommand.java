package com.freightos.admin.application.user.command;

import java.util.List;

public record SaveUserChangesCommand(
        List<CreateUserCommand> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record UpdateEntry(Long id, UpdateUserCommand command) {}
}
