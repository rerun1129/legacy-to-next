package com.freightos.fms.adapter.out.persistence.blquicksearch;

import com.freightos.fms.application.blquicksearch.port.out.BlQuickSearchPort;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BlQuickSearchPersistenceAdapter implements BlQuickSearchPort {

    private final BlQuickSearchRepositoryCustom blQuickSearchRepositoryCustom;

    @Override
    public List<BlQuickSearchSummary> searchHouse(BlQuickSearchFilter filter, int limit) {
        return blQuickSearchRepositoryCustom.searchHouse(filter, limit);
    }

    @Override
    public List<BlQuickSearchSummary> searchMaster(BlQuickSearchFilter filter, int limit) {
        return blQuickSearchRepositoryCustom.searchMaster(filter, limit);
    }
}
