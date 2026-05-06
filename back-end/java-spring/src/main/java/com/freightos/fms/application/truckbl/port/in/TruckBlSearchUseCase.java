package com.freightos.fms.application.truckbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;

public interface TruckBlSearchUseCase {
    PagedResult<TruckBlSummary> searchTruckBls(SearchTruckBlCommand cmd, PageRequest pageRequest);
}
