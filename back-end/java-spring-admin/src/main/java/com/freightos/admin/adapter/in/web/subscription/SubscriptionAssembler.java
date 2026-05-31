package com.freightos.admin.adapter.in.web.subscription;

import com.freightos.admin.adapter.in.web.subscription.dto.SaveSubscriptionChangesRequest;
import com.freightos.admin.adapter.in.web.subscription.dto.SubscriptionItemResponse;
import com.freightos.admin.application.subscription.command.SaveSubscriptionChangesCommand;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionAssembler {

    public SubscriptionItemResponse toResponse(SubscriptionSummary p) {
        return new SubscriptionItemResponse(p.id(), p.subscriberId(), p.moduleCode(),
                p.startDate(), p.endDate(), p.active(), p.createdAt(), p.updatedAt());
    }

    public SaveSubscriptionChangesCommand toSaveChangesCommand(Long subscriberId, SaveSubscriptionChangesRequest req) {
        List<SaveSubscriptionChangesCommand.CreateEntry> creates = req.creates() == null ? List.of()
                : req.creates().stream()
                        .map(c -> new SaveSubscriptionChangesCommand.CreateEntry(c.moduleCode(), c.startDate(), c.endDate(), c.active()))
                        .toList();
        List<SaveSubscriptionChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveSubscriptionChangesCommand.UpdateEntry(u.id(), u.startDate(), u.endDate(), u.active()))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveSubscriptionChangesCommand(subscriberId, creates, updates, deleteIds);
    }
}
