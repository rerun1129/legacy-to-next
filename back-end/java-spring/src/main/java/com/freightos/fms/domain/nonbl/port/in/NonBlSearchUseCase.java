package com.freightos.fms.domain.nonbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;

public interface NonBlSearchUseCase {
    PagedResult<NonBlSummary> searchNonBls(NonBlFilter filter, PageRequest pageRequest);
}
