package com.freightos.fms.application.truckbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlListItem;

public interface TruckBlSearchUseCase {
    PagedResult<TruckBlListItem> searchTruckBls(SearchTruckBlCommand cmd, PageRequest pageRequest);
}
