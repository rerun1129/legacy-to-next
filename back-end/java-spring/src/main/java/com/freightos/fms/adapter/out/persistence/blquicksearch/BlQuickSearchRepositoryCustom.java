package com.freightos.fms.adapter.out.persistence.blquicksearch;

import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;

import java.util.List;

public interface BlQuickSearchRepositoryCustom {

    List<BlQuickSearchSummary> searchHouse(BlQuickSearchFilter filter, int limit);

    List<BlQuickSearchSummary> searchMaster(BlQuickSearchFilter filter, int limit);
}
