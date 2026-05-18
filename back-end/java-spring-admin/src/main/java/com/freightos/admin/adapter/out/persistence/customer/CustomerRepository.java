package com.freightos.admin.adapter.out.persistence.customer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerJpaEntity, Long>, CustomerRepositoryCustom {
}
