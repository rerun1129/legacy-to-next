package com.freightos.admin.adapter.out.persistence.code.country;

import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.port.out.CountryPort;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.country.entity.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CountryPersistenceAdapter implements CountryPort {

    private final CountryRepository countryRepository;
    private final CountryDomainToJpaMapper domainToJpaMapper;
    private final CountryJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<CountrySummary> searchSummaries(SearchCountryCommand command) {
        return countryRepository.searchSummaries(command);
    }

    @Override
    public Optional<Country> findById(Long id) {
        return countryRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Country country) {
        CountryJpaEntity entity = domainToJpaMapper.toNewJpa(country);
        countryRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, Country patchData) {
        CountryJpaEntity entity = countryRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("COUNTRY_NOT_FOUND", MessageCode.COUNTRY_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        CountryJpaEntity entity = countryRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("COUNTRY_NOT_FOUND", MessageCode.COUNTRY_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return countryRepository.autocomplete(query, limit);
    }
}
