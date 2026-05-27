package com.freightos.admin.application.code.country.port.in;

import com.freightos.admin.application.code.country.command.CreateCountryCommand;
import com.freightos.admin.application.code.country.command.SaveCountryChangesCommand;
import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.command.UpdateCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.country.entity.Country;

import java.util.List;

public interface CountryUseCase {
    PagedResult<CountrySummary> searchCountries(SearchCountryCommand command);
    Country getCountryById(Long id);
    Long createCountry(CreateCountryCommand command);
    void updateCountry(Long id, UpdateCountryCommand command);
    void deleteCountry(Long id);
    void deleteCountries(List<Long> ids);
    SaveChangesResult saveCountryChanges(SaveCountryChangesCommand command);
    List<AutocompleteItem> autocompleteCountries(String query, int limit);
}
