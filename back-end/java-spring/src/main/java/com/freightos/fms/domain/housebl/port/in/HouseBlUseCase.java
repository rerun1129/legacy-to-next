package com.freightos.fms.domain.housebl.port.in;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.util.UUID;

public interface HouseBlUseCase {
    PagedResult<HouseBl> list(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    HouseBl getById(UUID id);
    HouseBl save(HouseBl houseBl);
    void delete(UUID id);
}
