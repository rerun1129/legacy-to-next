package com.freightos.fms.domain.masterbl.port.in;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.util.UUID;

public interface MasterBlUseCase {
    PagedResult<MasterBl> list(Bound bound, PageRequest pageRequest);
    MasterBl getById(UUID id);
    void delete(UUID id);
}
