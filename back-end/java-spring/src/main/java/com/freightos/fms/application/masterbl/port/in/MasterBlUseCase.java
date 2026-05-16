package com.freightos.fms.application.masterbl.port.in;

import com.freightos.fms.application.masterbl.command.ChangeMasterBlNoCommand;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;

import java.util.List;

public interface MasterBlUseCase {
    PagedResult<MasterBlSummaryResult> searchMasterBls(SearchMasterBlCommand cmd, PageRequest pageRequest);
    MasterBlDetailResult findMasterBlById(Long id);
    Long createMasterBl(CreateMasterBlCommand command);
    MasterBlDetailResult updateMasterBl(Long id, UpdateMasterBlCommand command);
    void deleteMasterBlById(Long id);
    List<Long> findMasterBlKeysByMblNoExact(String mblNo);
    void changeMblNo(Long id, ChangeMasterBlNoCommand command);
}
