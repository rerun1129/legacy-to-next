package com.freightos.fms.application.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.domain.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.domain.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.domain.truckbl.projection.TruckBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TruckBlSearchService implements TruckBlSearchUseCase {

    private final TruckBlSearchPort truckBlSearchPort;

    @Override
    public PagedResult<TruckBlSummary> searchTruckBls(TruckBlFilter filter, PageRequest pageRequest) {
        return truckBlSearchPort.searchTruckBlSummaries(filter, pageRequest);
    }
}
