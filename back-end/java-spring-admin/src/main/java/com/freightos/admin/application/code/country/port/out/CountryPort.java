package com.freightos.admin.application.code.country.port.out;

import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.country.entity.Country;

import java.util.List;
import java.util.Optional;

public interface CountryPort {
    PagedResult<CountrySummary> searchSummaries(SearchCountryCommand command);
    Optional<Country> findById(Long id);
    Long save(Country country);
    void update(Long id, Country patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
