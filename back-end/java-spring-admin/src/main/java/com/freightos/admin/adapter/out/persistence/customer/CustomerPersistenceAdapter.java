package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.application.customer.command.SearchCustomerCommand;
import com.freightos.admin.application.customer.port.out.CustomerPort;
import com.freightos.admin.application.customer.projection.CustomerSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.customer.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerPort {

    private final CustomerRepository customerRepository;
    private final CustomerDomainToJpaMapper domainToJpaMapper;
    private final CustomerJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<CustomerSummary> searchSummaries(SearchCustomerCommand command) {
        return customerRepository.searchSummaries(command);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Customer customer) {
        CustomerJpaEntity entity = domainToJpaMapper.toNewJpa(customer);
        customerRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Customer patchData) {
        CustomerJpaEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CUSTOMER_NOT_FOUND", MessageCode.CUSTOMER_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        CustomerJpaEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CUSTOMER_NOT_FOUND", MessageCode.CUSTOMER_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }
}
