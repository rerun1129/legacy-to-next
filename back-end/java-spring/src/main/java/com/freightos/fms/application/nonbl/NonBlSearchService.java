package com.freightos.fms.application.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.application.nonbl.port.in.NonBlSearchUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlSearchService implements NonBlSearchUseCase {

    private final NonBlSearchPort nonBlSearchPort;

    @Override
    public PagedResult<NonBlSummary> searchNonBls(NonBlFilter filter, PageRequest pageRequest) {
        return nonBlSearchPort.searchNonBlSummaries(filter, pageRequest);
    }
}
