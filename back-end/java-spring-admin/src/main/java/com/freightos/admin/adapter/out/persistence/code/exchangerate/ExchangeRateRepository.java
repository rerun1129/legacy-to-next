package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRateJpaEntity, Long>, ExchangeRateRepositoryCustom {

    Optional<ExchangeRateJpaEntity> findByFromCurrencyCodeAndToCurrencyCodeAndExchangeDateAndDeletedAtIsNull(
            String fromCurrencyCode, String toCurrencyCode, String exchangeDate);
}
