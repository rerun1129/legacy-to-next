package com.freightos.admin.application.customer.port.in;

import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.command.UpdateCustomerCommand;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;

import java.util.List;

public interface CustomerUseCase {
    PagedResult<CustomerSummary> searchCustomers(SearchCustomerCommand command);
    Customer getCustomerById(Long id);
    Long createCustomer(CreateCustomerCommand command);
    void updateCustomer(Long id, UpdateCustomerCommand command);
    void deleteCustomer(Long id);
    void deleteCustomers(List<Long> ids);
}
