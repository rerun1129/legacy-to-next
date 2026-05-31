package com.freightos.fms.application.seamaster.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.seamaster.command.SearchSeaMasterCommand;
import com.freightos.fms.application.seamaster.projection.SeaMasterListItem;

public interface SeaMasterSearchUseCase {
    PagedResult<SeaMasterListItem> searchSeaMasters(SearchSeaMasterCommand cmd, PageRequest pageRequest);
}
