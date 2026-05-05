package com.freightos.fms.application.housebl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

public interface HouseBlUseCase {
    PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest);
    HouseBlDetailResult findHouseBlById(Long id);
    Long createHouseBl(CreateHouseBlCommand command);
    HouseBlDetailResult updateHouseBl(Long id, UpdateHouseBlCommand command);
    void deleteHouseBlById(Long id);
}
