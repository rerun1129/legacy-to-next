package com.freightos.fms.application.masterbl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.util.List;
import java.util.Optional;

public interface MasterBlPort {
    Optional<MasterBl> findMasterBlById(Long id);
    PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest);
    PagedResult<MasterBlSummaryResult> searchMasterBls(MasterBlFilter filter, PageRequest pageRequest);
    Optional<MasterBl> findMasterBlByMblNo(String mblNo);
    boolean existsByMblNo(String mblNo);
    MasterBl saveMasterBl(MasterBl domain);
    Optional<MasterBlJobDiv> findJobDivById(Long id);
    void deleteByIdAndJobDiv(Long id, MasterBlJobDiv jobDiv);
    List<Long> findMasterBlKeysByMblNoExact(String mblNo);
}
