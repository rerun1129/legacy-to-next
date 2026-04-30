package com.freightos.fms.domain.masterbl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.util.Optional;

public interface MasterBlPort {
    Optional<MasterBl> findMasterBlById(Long id);
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    Optional<MasterBl> findMasterBlByMblNo(String mblNo);
    boolean existsByMblNo(String mblNo);
    MasterBl saveMasterBl(MasterBl domain);
    void deleteMasterBl(MasterBl masterBl);
}
