package com.freightos.fms.domain.housebl.port.in;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

public interface HouseBlUseCase {
    PagedResult<HouseBlSummary> getHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    HouseBl findHouseBlById(Long id);
    HouseBl save(HouseBl houseBl);
    void deleteHouseBlById(Long id);
}
