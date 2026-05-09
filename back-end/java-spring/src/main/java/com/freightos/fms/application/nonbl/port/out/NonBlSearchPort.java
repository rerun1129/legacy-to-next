package com.freightos.fms.application.nonbl.port.out;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;

import java.util.Optional;

public interface NonBlSearchPort {
    PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest);
    Optional<HouseBlNonBl> findNonBlById(Long id);
}
