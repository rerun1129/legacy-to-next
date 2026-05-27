package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface CustomerRepositoryCustom {
    PagedResult<CustomerSummary> searchSummaries(SearchCustomerCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
