package com.freightos.admin.application.subscriber.port.out;

import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.subscriber.entity.Subscriber;

import java.util.List;
import java.util.Optional;

public interface SubscriberPort {
    PagedResult<SubscriberSummary> searchSummaries(SearchSubscriberCommand command);
    Optional<Subscriber> findById(Long id);
    Optional<Subscriber> findBySubscriberCode(String subscriberCode);
    Long save(Subscriber subscriber);
    void update(Long id, Subscriber patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
