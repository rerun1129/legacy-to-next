package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;

import java.util.List;

public interface MasterBlRepositoryCustom {
    PagedResult<MasterBlSummaryResult> searchByFilter(MasterBlFilter filter, PageRequest pageRequest);
    List<Long> findMasterBlKeysByMblNoExact(String mblNo);
}
