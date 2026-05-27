package com.freightos.admin.adapter.out.persistence.code.country;

import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
import com.freightos.admin.common.response.PagedResult;

public interface CountryRepositoryCustom {
    PagedResult<CountrySummary> searchSummaries(SearchCountryCommand command);
}
