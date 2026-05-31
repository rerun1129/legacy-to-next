package com.freightos.admin.application.subscription.command;

import java.time.LocalDate;
import java.util.List;

public record SaveSubscriptionChangesCommand(
        Long subscriberId,
        List<CreateEntry> creates,
        List<UpdateEntry> updates,
        List<Long> deleteIds
) {
    public record CreateEntry(String moduleCode, LocalDate startDate, LocalDate endDate, boolean active) {}
    public record UpdateEntry(Long id, LocalDate startDate, LocalDate endDate, boolean active) {}
}
