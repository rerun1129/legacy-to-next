package com.freightos.fms.adapter.out.persistence.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;

import java.util.List;

public interface TruckBlRepositoryCustom {
    PagedResult<TruckBlSummary> searchTruckBlSummaries(TruckBlFilter filter, PageRequest pageRequest);
    List<Long> findTruckBlKeysByHblNoExact(String hblNo);
}
