package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;

public interface MasterBlRepositoryCustom {
    PagedResult<MasterBlSummaryResult> searchByFilter(MasterBlFilter filter, PageRequest pageRequest);
}
