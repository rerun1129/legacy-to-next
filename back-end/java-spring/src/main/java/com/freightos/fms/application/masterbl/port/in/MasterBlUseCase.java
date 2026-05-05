package com.freightos.fms.application.masterbl.port.in;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

public interface MasterBlUseCase {
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    PagedResult<MasterBl> searchMasterBls(MasterBlFilter filter, PageRequest pageRequest);
    MasterBlDetailResult findMasterBlById(Long id);
    MasterBlDetailResult createMasterBl(CreateMasterBlCommand command);
    MasterBlDetailResult updateMasterBl(Long id, UpdateMasterBlCommand command);
    void deleteMasterBlById(Long id);
}
