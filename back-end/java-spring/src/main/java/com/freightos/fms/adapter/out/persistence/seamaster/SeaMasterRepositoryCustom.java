package com.freightos.fms.adapter.out.persistence.seamaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;

public interface SeaMasterRepositoryCustom {
    PagedResult<SeaMasterSummary> searchSeaMasterSummaries(SeaMasterFilter filter, PageRequest pageRequest);
}
