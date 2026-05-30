package com.freightos.fms.application.nonbl.port.in;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;

import java.util.List;

public interface NonBlUseCase {
    PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest);
    NonBlDetailView findNonBlById(Long id);
    Long createNonBl(CreateHouseBlCommand command);
    void updateNonBl(Long id, UpdateHouseBlCommand command);
    void deleteNonBlById(Long id);
    void changeNonBlHblNo(Long id, ChangeHouseBlNoCommand command);
    List<Long> findNonBlKeysByHblNoExact(String hblNo);
}
