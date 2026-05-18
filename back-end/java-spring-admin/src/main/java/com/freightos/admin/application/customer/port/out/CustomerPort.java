package com.freightos.admin.application.customer.port.out;

import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;

import java.util.Optional;

public interface CustomerPort {
    PagedResult<CustomerSummary> searchSummaries(SearchCustomerCommand command);
    Optional<Customer> findById(Long id);
    Long save(Customer customer);
    void update(Long id, Customer patchData);
    void softDelete(Long id);
}
