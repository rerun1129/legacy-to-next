package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateJpaEntity, Long>, ExchangeRateRepositoryCustom {
}
