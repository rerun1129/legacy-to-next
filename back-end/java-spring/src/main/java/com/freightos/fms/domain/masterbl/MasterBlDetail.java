package com.freightos.fms.domain.masterbl;

import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

import java.util.List;

public record MasterBlDetail(
        MasterBl masterBl,
        List<ConsoledHouseBlSummary> consolidatedHouseBls
) {
}
