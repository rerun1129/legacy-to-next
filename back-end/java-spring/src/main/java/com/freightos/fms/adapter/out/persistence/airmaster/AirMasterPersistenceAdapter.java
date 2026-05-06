package com.freightos.fms.adapter.out.persistence.airmaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.application.airmaster.port.out.AirMasterSearchPort;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AirMasterPersistenceAdapter implements AirMasterSearchPort {

    private final AirMasterRepositoryCustom airMasterRepositoryCustom;

    @Override
    public PagedResult<AirMasterSummary> searchAirMasterSummaries(AirMasterFilter filter, PageRequest pageRequest) {
        return airMasterRepositoryCustom.searchAirMasterSummaries(filter, pageRequest);
    }
}
