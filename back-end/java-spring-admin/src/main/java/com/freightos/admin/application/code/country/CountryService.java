package com.freightos.admin.application.code.country;

import com.freightos.admin.application.code.country.command.CreateCountryCommand;
import com.freightos.admin.application.code.country.command.SaveCountryChangesCommand;
import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.command.UpdateCountryCommand;
import com.freightos.admin.application.code.country.port.in.CountryUseCase;
import com.freightos.admin.application.code.country.port.out.CountryPort;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.country.entity.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryService implements CountryUseCase {

    private final CountryPort countryPort;
    private final CountryFactory countryFactory;

    @Override
    public PagedResult<CountrySummary> searchCountries(SearchCountryCommand command) {
        return countryPort.searchSummaries(command);
    }

    @Override
    public Country getCountryById(Long id) {
        return countryPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("COUNTRY_NOT_FOUND", MessageCode.COUNTRY_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCountry(CreateCountryCommand command) {
        try {
            return countryPort.save(countryFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("COUNTRY_DUPLICATE_CODE", MessageCode.COUNTRY_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCountry(Long id, UpdateCountryCommand command) {
        Country country = getCountryById(id);
        if (country.isDeleted()) {
            throw ApplicationException.conflict("COUNTRY_ALREADY_DELETED", MessageCode.COUNTRY_ALREADY_DELETED.getMessage());
        }
        country.applyUpdate(command.name(), command.nameEn(), command.active());
        countryPort.update(id, country);
    }

    @Override
    @Transactional
    public void deleteCountry(Long id) {
        Country country = getCountryById(id);
        if (country.isDeleted()) {
            throw ApplicationException.conflict("COUNTRY_ALREADY_DELETED", MessageCode.COUNTRY_ALREADY_DELETED.getMessage());
        }
        countryPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteCountries(List<Long> ids) {
        for (Long id : ids) {
            deleteCountry(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveCountryChanges(SaveCountryChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteCountry(id);
        }
        for (SaveCountryChangesCommand.UpdateEntry entry : command.updates()) {
            updateCountry(entry.id(), entry.command());
        }
        for (CreateCountryCommand create : command.creates()) {
            createCountry(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }
}
