package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;

public interface MasterBlRepositoryCustom {
    PagedResult<MasterBlJpaEntity> searchByFilter(MasterBlFilter filter, PageRequest pageRequest);
}
