package com.freightos.fms.application.seamaster.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;

public interface SeaMasterSearchPort {
    PagedResult<SeaMasterSummary> searchSeaMasterSummaries(SeaMasterFilter filter, PageRequest pageRequest);
}
