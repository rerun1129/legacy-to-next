package com.freightos.admin.adapter.out.persistence.customer;

import com.freightos.admin.domain.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerJpaToDomainMapper {

    public Customer toDomain(CustomerJpaEntity e) {
        Customer domain = Customer.create(
                e.getCustomerCode(), e.getCustomerType(), e.getName(), e.getNameEn(),
                e.getBusinessNo(), e.getRepresentative(), e.getPhone(), e.getEmail(),
                e.getCustomerLocalAddress(), e.getCustomerEnglishAddress(), e.getMemo(), e.getActive()
        );
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
