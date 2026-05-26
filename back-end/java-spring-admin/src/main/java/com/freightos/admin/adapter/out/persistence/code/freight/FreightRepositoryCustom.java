package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.response.PagedResult;

public interface FreightRepositoryCustom {
    PagedResult<FreightSummary> searchSummaries(SearchFreightCommand command);
}
