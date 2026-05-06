package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import org.springframework.stereotype.Component;

@Component
public class NonBlAssembler {

    public PagedResult<NonBlSummaryResponse> toSummaryPage(PagedResult<NonBlSummary> source) {
        return source.map(NonBlSummaryResponse::from);
    }

    public SearchNonBlCommand toSearchCommand(SearchNonBlRequest req) {
        return new SearchNonBlCommand(
                req.bound() != null ? req.bound().name() : null,
                req.hblNo(),
                req.etdFrom(), req.etdTo(),
                req.linerCode(),
                req.partyCode(), req.portCode(),
                req.vessel(), req.voyage(),
                req.operatorCode(), req.teamCode(),
                req.dateKind() != null ? req.dateKind().name() : null,
                req.partyKind() != null ? req.partyKind().name() : null,
                req.portKind() != null ? req.portKind().name() : null
        );
    }
}
