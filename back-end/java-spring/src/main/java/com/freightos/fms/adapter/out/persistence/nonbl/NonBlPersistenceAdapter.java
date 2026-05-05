package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NonBlPersistenceAdapter implements NonBlSearchPort {

    private final NonBlRepositoryCustom nonBlRepositoryCustom;

    @Override
    public PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest) {
        return nonBlRepositoryCustom.searchNonBlSummaries(filter, pageRequest);
    }
}
