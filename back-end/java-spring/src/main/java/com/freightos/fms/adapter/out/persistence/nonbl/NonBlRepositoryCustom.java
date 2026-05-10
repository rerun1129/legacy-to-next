package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;

import java.util.List;
import java.util.Optional;

public interface NonBlRepositoryCustom {
    PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest);
    Optional<HouseBlNonBl> findNonBlById(Long id);
    List<Long> findNonBlKeysByHblNoExact(String hblNo);
}
