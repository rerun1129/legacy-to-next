package com.freightos.fms.adapter.out.persistence.airhouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;

public interface AirHouseRepositoryCustom {
    PagedResult<AirHouseSummary> searchAirHouseSummaries(AirHouseFilter filter, PageRequest pageRequest);
}
