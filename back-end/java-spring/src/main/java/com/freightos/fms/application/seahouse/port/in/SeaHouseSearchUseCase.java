package com.freightos.fms.application.seahouse.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.seahouse.command.SearchSeaHouseCommand;
import com.freightos.fms.application.seahouse.projection.SeaHouseListItem;

public interface SeaHouseSearchUseCase {
    PagedResult<SeaHouseListItem> searchSeaHouses(SearchSeaHouseCommand cmd, PageRequest pageRequest);
}
