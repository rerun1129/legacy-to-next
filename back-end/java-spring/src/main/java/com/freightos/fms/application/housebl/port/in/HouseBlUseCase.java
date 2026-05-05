package com.freightos.fms.application.housebl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

import java.util.function.Consumer;

public interface HouseBlUseCase {
    PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest);
    HouseBl findHouseBlById(Long id);
    Long createHouseBl(HouseBl houseBl);
    HouseBl updateHouseBl(Long id, Consumer<HouseBl> patcher);
    void deleteHouseBlById(Long id);
}
