package com.freightos.fms.application.truckbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;

public interface TruckBlSearchUseCase {
    PagedResult<TruckBlSummary> searchTruckBls(TruckBlFilter filter, PageRequest pageRequest);
}
