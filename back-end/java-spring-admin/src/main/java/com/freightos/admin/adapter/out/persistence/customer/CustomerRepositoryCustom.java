package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.PagedResult;

public interface CustomerRepositoryCustom {
    PagedResult<CustomerSummary> searchSummaries(SearchCustomerCommand command);
}
