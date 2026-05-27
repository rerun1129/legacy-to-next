package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface ExchangeRateRepositoryCustom {
    PagedResult<ExchangeRateSummary> searchSummaries(SearchExchangeRateCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
