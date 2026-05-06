package com.freightos.fms.application.airmaster.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airmaster.command.SearchAirMasterCommand;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;

public interface AirMasterSearchUseCase {
    PagedResult<AirMasterSummary> searchAirMasters(SearchAirMasterCommand cmd, PageRequest pageRequest);
}
