package com.freightos.fms.domain.masterbl.port.in;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

public interface MasterBlUseCase {
    PagedResult<MasterBl> list(Bound bound, PageRequest pageRequest);
    MasterBl getById(Long id);
    void delete(Long id);
}
