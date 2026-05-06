package com.freightos.fms.application.airhouse.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;

public interface AirHouseSearchPort {
    PagedResult<AirHouseSummary> searchAirHouseSummaries(AirHouseFilter filter, PageRequest pageRequest);
}
