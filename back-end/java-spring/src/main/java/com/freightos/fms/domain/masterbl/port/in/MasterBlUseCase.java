package com.freightos.fms.domain.masterbl.port.in;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

public interface MasterBlUseCase {
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    MasterBl findMasterBlById(Long id);
    void deleteMasterBlById(Long id);
}
