package com.freightos.fms.application.airmaster.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;

public interface AirMasterSearchPort {
    PagedResult<AirMasterSummary> searchAirMasterSummaries(AirMasterFilter filter, PageRequest pageRequest);
}
