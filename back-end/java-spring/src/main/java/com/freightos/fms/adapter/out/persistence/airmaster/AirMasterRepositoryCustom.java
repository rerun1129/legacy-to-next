package com.freightos.fms.adapter.out.persistence.airmaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;

public interface AirMasterRepositoryCustom {
    PagedResult<AirMasterSummary> searchAirMasterSummaries(AirMasterFilter filter, PageRequest pageRequest);
}
