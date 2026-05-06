package com.freightos.fms.application.nonbl.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;

public interface NonBlSearchPort {
    PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest);
}
