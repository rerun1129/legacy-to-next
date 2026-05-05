package com.freightos.fms.adapter.out.persistence.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.domain.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.domain.truckbl.projection.TruckBlSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TruckBlPersistenceAdapter implements TruckBlSearchPort {

    private final TruckBlRepositoryCustom truckBlRepositoryCustom;

    @Override
    public PagedResult<TruckBlSummary> searchTruckBlSummaries(TruckBlFilter filter, PageRequest pageRequest) {
        return truckBlRepositoryCustom.searchTruckBlSummaries(filter, pageRequest);
    }
}
