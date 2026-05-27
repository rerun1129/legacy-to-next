package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.port.out.CurrencyPort;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.currency.entity.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrencyPersistenceAdapter implements CurrencyPort {

    private final CurrencyRepository currencyRepository;
    private final CurrencyDomainToJpaMapper domainToJpaMapper;
    private final CurrencyJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<CurrencySummary> searchSummaries(SearchCurrencyCommand command) {
        return currencyRepository.searchSummaries(command);
    }

    @Override
    public Optional<Currency> findById(Long id) {
        return currencyRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Currency currency) {
        CurrencyJpaEntity entity = domainToJpaMapper.toNewJpa(currency);
        currencyRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Currency patchData) {
        CurrencyJpaEntity entity = currencyRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CURRENCY_NOT_FOUND", MessageCode.CURRENCY_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        CurrencyJpaEntity entity = currencyRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CURRENCY_NOT_FOUND", MessageCode.CURRENCY_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return currencyRepository.autocomplete(query, limit);
    }
}
