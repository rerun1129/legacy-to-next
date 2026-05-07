package com.freightos.fms.adapter.out.persistence.seamaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.application.seamaster.port.out.SeaMasterSearchPort;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeaMasterPersistenceAdapter implements SeaMasterSearchPort {

    private final SeaMasterRepositoryCustom seaMasterRepositoryCustom;

    @Override
    public PagedResult<SeaMasterSummary> searchSeaMasterSummaries(SeaMasterFilter filter, PageRequest pageRequest) {
        return seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, pageRequest);
    }
}
