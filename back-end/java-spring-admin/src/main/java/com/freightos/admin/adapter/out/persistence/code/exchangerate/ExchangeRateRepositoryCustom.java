package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
import com.freightos.admin.common.response.PagedResult;

public interface ExchangeRateRepositoryCustom {
    PagedResult<ExchangeRateSummary> searchSummaries(SearchExchangeRateCommand command);
}
