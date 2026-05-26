package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.port.out.ExchangeRatePort;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ExchangeRatePersistenceAdapter implements ExchangeRatePort {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateDomainToJpaMapper domainToJpaMapper;
    private final ExchangeRateJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<ExchangeRateSummary> searchSummaries(SearchExchangeRateCommand command) {
        return exchangeRateRepository.searchSummaries(command);
    }

    @Override
    public Optional<ExchangeRate> findById(Long id) {
        return exchangeRateRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(ExchangeRate exchangeRate) {
        ExchangeRateJpaEntity entity = domainToJpaMapper.toNewJpa(exchangeRate);
        exchangeRateRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, ExchangeRate patchData) {
        ExchangeRateJpaEntity entity = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("EXCHANGE_RATE_NOT_FOUND", MessageCode.EXCHANGE_RATE_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        ExchangeRateJpaEntity entity = exchangeRateRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("EXCHANGE_RATE_NOT_FOUND", MessageCode.EXCHANGE_RATE_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }
}
