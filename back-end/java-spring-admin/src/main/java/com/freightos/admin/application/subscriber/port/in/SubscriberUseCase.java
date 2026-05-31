package com.freightos.admin.application.subscriber.port.in;

import com.freightos.admin.application.subscriber.command.CreateSubscriberCommand;
import com.freightos.admin.application.subscriber.command.SaveSubscriberChangesCommand;
import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.command.UpdateSubscriberCommand;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;

import java.util.List;

public interface SubscriberUseCase {
    PagedResult<SubscriberSummary> searchSubscribers(SearchSubscriberCommand command);
    Subscriber getSubscriberById(Long id);
    Long createSubscriber(CreateSubscriberCommand command);
    void updateSubscriber(Long id, UpdateSubscriberCommand command);
    void deleteSubscriber(Long id);
    SaveChangesResult saveSubscriberChanges(SaveSubscriberChangesCommand command);
    List<AutocompleteItem> autocompleteSubscribers(String query, int limit);
}
