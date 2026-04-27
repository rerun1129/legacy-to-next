package com.freightos.fms.domain.masterbl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.util.Optional;

public interface MasterBlPort {
    Optional<MasterBl> findById(Long id);
    PagedResult<MasterBl> findAllByBound(Bound bound, PageRequest pageRequest);
    Optional<MasterBl> findByMblNo(String mblNo);
    boolean existsByMblNo(String mblNo);
    void delete(MasterBl masterBl);
}
