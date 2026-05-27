package com.freightos.admin.application.code.exchangerate.port.out;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.exchangerate.entity.ExchangeRate;

import java.util.List;
import java.util.Optional;

public interface ExchangeRatePort {
    PagedResult<ExchangeRateSummary> searchSummaries(SearchExchangeRateCommand command);
    Optional<ExchangeRate> findById(Long id);
    Long save(ExchangeRate exchangeRate);
    void update(Long id, ExchangeRate patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
