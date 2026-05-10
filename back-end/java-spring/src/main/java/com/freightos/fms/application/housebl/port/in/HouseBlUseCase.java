package com.freightos.fms.application.housebl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;

public interface HouseBlUseCase {
    PagedResult<HouseBlSummary> searchHouseBls(SearchHouseBlCommand cmd, PageRequest pageRequest);
    HouseBlDetailResult findHouseBlById(Long id);
    Long createHouseBl(CreateHouseBlCommand command);
    HouseBlDetailResult updateHouseBl(Long id, UpdateHouseBlCommand command);
    void deleteHouseBlById(Long id);
    void changeHblNo(Long id, ChangeHouseBlNoCommand command);
}
