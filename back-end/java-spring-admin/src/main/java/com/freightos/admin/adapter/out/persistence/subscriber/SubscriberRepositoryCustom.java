package com.freightos.admin.adapter.out.persistence.subscriber;

import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface SubscriberRepositoryCustom {
    PagedResult<SubscriberSummary> searchSummaries(SearchSubscriberCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
