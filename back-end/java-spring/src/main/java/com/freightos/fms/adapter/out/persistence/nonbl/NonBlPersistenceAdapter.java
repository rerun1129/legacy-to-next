package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NonBlPersistenceAdapter implements NonBlSearchPort {

    private final NonBlRepositoryCustom nonBlRepositoryCustom;

    @Override
    public PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest) {
        return nonBlRepositoryCustom.searchNonBlSummaries(filter, pageRequest);
    }

    @Override
    public Optional<HouseBlNonBl> findNonBlById(Long id) {
        return nonBlRepositoryCustom.findNonBlById(id);
    }
}
