package com.freightos.admin.application.subscription.port.in;

import com.freightos.admin.application.subscription.command.SaveSubscriptionChangesCommand;
import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.common.response.SaveChangesResult;

import java.util.List;

public interface SubscriptionUseCase {
    List<SubscriptionSummary> getSubscriptionsBySubscriberId(Long subscriberId);
    SaveChangesResult saveSubscriptionChanges(SaveSubscriptionChangesCommand command);
}
