package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

public interface HouseBlRepositoryCustom {
    PagedResult<HouseBlSummary> findSummariesByJobDivAndBound(
        JobDiv jobDiv, Bound bound, PageRequest pageRequest
    );
}
