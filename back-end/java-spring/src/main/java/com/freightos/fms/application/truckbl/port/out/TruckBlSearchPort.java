package com.freightos.fms.application.truckbl.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.domain.truckbl.projection.TruckBlSummary;

public interface TruckBlSearchPort {
    PagedResult<TruckBlSummary> searchTruckBlSummaries(TruckBlFilter filter, PageRequest pageRequest);
}
