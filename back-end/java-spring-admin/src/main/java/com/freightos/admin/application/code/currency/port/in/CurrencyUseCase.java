package com.freightos.admin.application.code.currency.port.in;

import com.freightos.admin.application.code.currency.command.CreateCurrencyCommand;
import com.freightos.admin.application.code.currency.command.SaveCurrencyChangesCommand;
import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.command.UpdateCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.currency.entity.Currency;

import java.util.List;

public interface CurrencyUseCase {
    PagedResult<CurrencySummary> searchCurrencies(SearchCurrencyCommand command);
    Currency getCurrencyById(Long id);
    Long createCurrency(CreateCurrencyCommand command);
    void updateCurrency(Long id, UpdateCurrencyCommand command);
    void deleteCurrency(Long id);
    void deleteCurrencies(List<Long> ids);
    SaveChangesResult saveCurrencyChanges(SaveCurrencyChangesCommand command);
    List<AutocompleteItem> autocompleteCurrencies(String query, int limit);
}
