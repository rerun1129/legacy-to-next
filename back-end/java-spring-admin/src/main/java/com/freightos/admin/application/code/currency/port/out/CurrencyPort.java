package com.freightos.admin.application.code.currency.port.out;

import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.currency.entity.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyPort {
    PagedResult<CurrencySummary> searchSummaries(SearchCurrencyCommand command);
    Optional<Currency> findById(Long id);
    Long save(Currency currency);
    void update(Long id, Currency patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
