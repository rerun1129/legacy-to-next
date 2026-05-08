package com.freightos.fms.application.nonbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;

public interface NonBlUseCase {
    PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest);
    NonBlDetailResult findNonBlById(Long id);
    Long createNonBl(CreateHouseBlCommand command);
    NonBlDetailResult updateNonBl(Long id, UpdateHouseBlCommand command);
    void deleteNonBlById(Long id);
}
