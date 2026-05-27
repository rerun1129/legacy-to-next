package com.freightos.admin.application.customer;

import com.freightos.admin.application.customer.command.CreateCustomerCommand;
import com.freightos.admin.application.customer.command.SaveCustomerChangesCommand;
import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.command.UpdateCustomerCommand;
import com.freightos.admin.application.customer.port.in.CustomerUseCase;
import com.freightos.admin.application.customer.port.out.CustomerPort;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements CustomerUseCase {

    private final CustomerPort customerPort;
    private final CustomerFactory customerFactory;

    @Override
    public PagedResult<CustomerSummary> searchCustomers(SearchCustomerCommand command) {
        return customerPort.searchSummaries(command);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CUSTOMER_NOT_FOUND", MessageCode.CUSTOMER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCustomer(CreateCustomerCommand command) {
        try {
            return customerPort.save(customerFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("CUSTOMER_DUPLICATE_CODE", MessageCode.CUSTOMER_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCustomer(Long id, UpdateCustomerCommand command) {
        Customer customer = getCustomerById(id);
        if (customer.isDeleted()) {
            throw ApplicationException.conflict("CUSTOMER_ALREADY_DELETED", MessageCode.CUSTOMER_ALREADY_DELETED.getMessage());
        }
        customer.applyUpdate(
                command.customerType(), command.name(), command.nameEn(),
                command.businessNo(), command.representative(), command.phone(),
                command.email(), command.customerLocalAddress(), command.customerEnglishAddress(), command.countryCode(), command.memo(), command.active()
        );
        customerPort.update(id, customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        if (customer.isDeleted()) {
            throw ApplicationException.conflict("CUSTOMER_ALREADY_DELETED", MessageCode.CUSTOMER_ALREADY_DELETED.getMessage());
        }
        customerPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteCustomers(List<Long> ids) {
        for (Long id : ids) {
            deleteCustomer(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveCustomerChanges(SaveCustomerChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteCustomer(id);
        }
        for (SaveCustomerChangesCommand.UpdateEntry entry : command.updates()) {
            updateCustomer(entry.id(), entry.command());
        }
        for (CreateCustomerCommand create : command.creates()) {
            createCustomer(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }

    @Override
    public List<AutocompleteItem> autocompleteCustomers(String query, int limit) {
        return customerPort.autocomplete(query, limit);
    }
}
