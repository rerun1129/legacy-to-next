package com.freightos.admin.application.code.country.port.in;

import com.freightos.admin.application.code.country.command.CreateCountryCommand;
import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.command.UpdateCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.country.entity.Country;

import java.util.List;

public interface CountryUseCase {
    PagedResult<CountrySummary> searchCountries(SearchCountryCommand command);
    Country getCountryById(Long id);
    Long createCountry(CreateCountryCommand command);
    void updateCountry(Long id, UpdateCountryCommand command);
    void deleteCountry(Long id);
    void deleteCountries(List<Long> ids);
}
