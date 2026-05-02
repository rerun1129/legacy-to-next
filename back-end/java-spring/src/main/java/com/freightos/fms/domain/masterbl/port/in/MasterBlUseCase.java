package com.freightos.fms.domain.masterbl.port.in;

import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

public interface MasterBlUseCase {
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    MasterBl findMasterBlById(Long id);
    MasterBl save(MasterBl masterBl);
    void deleteMasterBlById(Long id);
    MasterBlDetail findMasterBlDetailById(Long id);
    MasterBl save(MasterBl masterBl);
}
