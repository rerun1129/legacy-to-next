package com.freightos.fms.adapter.out.persistence.seahouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.application.seahouse.port.out.SeaHouseSearchPort;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeaHousePersistenceAdapter implements SeaHouseSearchPort {

    private final SeaHouseRepositoryCustom seaHouseRepositoryCustom;

    @Override
    public PagedResult<SeaHouseSummary> searchSeaHouseSummaries(SeaHouseFilter filter, PageRequest pageRequest) {
        return seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, pageRequest);
    }
}
