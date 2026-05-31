package com.freightos.fms.adapter.in.web.seamaster;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.seamaster.dto.SearchSeaMasterRequest;
import com.freightos.fms.adapter.in.web.seamaster.dto.SeaMasterSummaryResponse;
import com.freightos.fms.application.seamaster.command.SearchSeaMasterCommand;
import com.freightos.fms.application.seamaster.projection.SeaMasterListItem;
import org.springframework.stereotype.Component;

@Component
public class SeaMasterAssembler {

    public PagedResult<SeaMasterSummaryResponse> toSummaryPage(PagedResult<SeaMasterListItem> source) {
        return source.map(SeaMasterSummaryResponse::from);
    }

    public SearchSeaMasterCommand toSearchCommand(SearchSeaMasterRequest req) {
        return new SearchSeaMasterCommand(
                req.bound(),
                req.dateKind(),
                req.dateFrom(), req.dateTo(),
                req.masterBlKind(), req.masterBlValue(),
                req.partyKind(), req.partyCode(),
                req.linerCode(),
                req.portKind(), req.portCode(),
                req.vesselName(),
                req.voyageNo(),
                req.shipmentType(),
                req.loadType(),
                req.teamCode(),
                req.page(), req.size()
        );
    }
}
