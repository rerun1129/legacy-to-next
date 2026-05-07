package com.freightos.fms.adapter.out.persistence.seahouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;

public interface SeaHouseRepositoryCustom {
    PagedResult<SeaHouseSummary> searchSeaHouseSummaries(SeaHouseFilter filter, PageRequest pageRequest);
}
