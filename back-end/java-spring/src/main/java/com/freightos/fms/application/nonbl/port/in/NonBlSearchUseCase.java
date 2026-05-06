package com.freightos.fms.application.nonbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;

public interface NonBlSearchUseCase {
    PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest);
}
