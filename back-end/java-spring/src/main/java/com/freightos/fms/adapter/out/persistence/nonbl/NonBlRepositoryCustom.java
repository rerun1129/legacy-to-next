package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;

public interface NonBlRepositoryCustom {
    PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest);
}
