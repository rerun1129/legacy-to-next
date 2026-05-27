package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface CurrencyRepositoryCustom {
    PagedResult<CurrencySummary> searchSummaries(SearchCurrencyCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
