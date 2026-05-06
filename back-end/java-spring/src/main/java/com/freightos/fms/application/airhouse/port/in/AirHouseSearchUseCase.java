package com.freightos.fms.application.airhouse.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airhouse.command.SearchAirHouseCommand;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;

public interface AirHouseSearchUseCase {
    PagedResult<AirHouseSummary> searchAirHouses(SearchAirHouseCommand cmd, PageRequest pageRequest);
}
