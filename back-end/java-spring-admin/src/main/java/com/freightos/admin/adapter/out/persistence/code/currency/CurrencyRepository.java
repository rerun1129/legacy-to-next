package com.freightos.admin.adapter.out.persistence.code.currency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<CurrencyJpaEntity, Long>, CurrencyRepositoryCustom {
}
