package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.domain.truckbl.projection.TruckBlSummary;
import org.springframework.stereotype.Component;

@Component
public class TruckBlAssembler {
    public PagedResult<TruckBlSummaryResponse> toSummaryPage(PagedResult<TruckBlSummary> source) {
        return source.map(TruckBlSummaryResponse::from);
    }
}
