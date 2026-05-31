package com.freightos.fms.application.airhouse.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.airhouse.command.SearchAirHouseCommand;
import com.freightos.fms.application.airhouse.projection.AirHouseListItem;

public interface AirHouseSearchUseCase {
    PagedResult<AirHouseListItem> searchAirHouses(SearchAirHouseCommand cmd, PageRequest pageRequest);
}
