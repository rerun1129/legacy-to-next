package com.freightos.fms.adapter.out.persistence.airhouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.application.airhouse.port.out.AirHouseSearchPort;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AirHousePersistenceAdapter implements AirHouseSearchPort {

    private final AirHouseRepositoryCustom airHouseRepositoryCustom;

    @Override
    public PagedResult<AirHouseSummary> searchAirHouseSummaries(AirHouseFilter filter, PageRequest pageRequest) {
        return airHouseRepositoryCustom.searchAirHouseSummaries(filter, pageRequest);
    }
}
