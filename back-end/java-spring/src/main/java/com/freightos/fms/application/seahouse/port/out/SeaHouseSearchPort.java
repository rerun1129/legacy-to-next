package com.freightos.fms.application.seahouse.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;

public interface SeaHouseSearchPort {
    PagedResult<SeaHouseSummary> searchSeaHouseSummaries(SeaHouseFilter filter, PageRequest pageRequest);
}
