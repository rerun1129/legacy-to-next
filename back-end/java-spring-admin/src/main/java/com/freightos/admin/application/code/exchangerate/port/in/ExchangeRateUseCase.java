package com.freightos.admin.application.code.exchangerate.port.in;

import com.freightos.admin.application.code.exchangerate.command.CreateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.SaveExchangeRateChangesCommand;
import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.command.UpdateExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateValue;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;

import java.util.List;

public interface ExchangeRateUseCase {
    PagedResult<ExchangeRateSummary> searchExchangeRates(SearchExchangeRateCommand command);
    ExchangeRate getExchangeRateById(Long id);
    Long createExchangeRate(CreateExchangeRateCommand command);
    void updateExchangeRate(Long id, UpdateExchangeRateCommand command);
    void deleteExchangeRate(Long id);
    void deleteExchangeRates(List<Long> ids);
    SaveChangesResult saveExchangeRateChanges(SaveExchangeRateChangesCommand command);
    List<AutocompleteItem> autocompleteExchangeRates(String query, int limit);
    List<ExchangeRateValue> findRates(String fromCurrencyCode, String toCurrencyCode, String exchangeDate);
}
