package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;
import org.springframework.stereotype.Component;

@Component
public class NonBlAssembler {
    public PagedResult<NonBlSummaryResponse> toSummaryPage(PagedResult<NonBlSummary> source) {
        return source.map(NonBlSummaryResponse::from);
    }
}
