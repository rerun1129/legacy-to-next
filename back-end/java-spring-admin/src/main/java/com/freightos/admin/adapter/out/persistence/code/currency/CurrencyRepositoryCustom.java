package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
import com.freightos.admin.common.response.PagedResult;

public interface CurrencyRepositoryCustom {
    PagedResult<CurrencySummary> searchSummaries(SearchCurrencyCommand command);
}
