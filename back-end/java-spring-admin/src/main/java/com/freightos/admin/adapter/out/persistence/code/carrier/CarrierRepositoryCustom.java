package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.response.PagedResult;

public interface CarrierRepositoryCustom {
    PagedResult<CarrierSummary> searchSummaries(SearchCarrierCommand command);
}
