package com.freightos.fms.adapter.in.web.airmaster;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.airmaster.dto.SearchAirMasterRequest;
import com.freightos.fms.adapter.in.web.airmaster.dto.AirMasterSummaryResponse;
import com.freightos.fms.application.airmaster.command.SearchAirMasterCommand;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;
import org.springframework.stereotype.Component;

@Component
public class AirMasterAssembler {

    public PagedResult<AirMasterSummaryResponse> toSummaryPage(PagedResult<AirMasterSummary> source) {
        return source.map(AirMasterSummaryResponse::from);
    }

    public SearchAirMasterCommand toSearchCommand(SearchAirMasterRequest req) {
        return new SearchAirMasterCommand(
                req.bound(),
                req.dateKind(),
                req.dateFrom(), req.dateTo(),
                req.masterAwbKind(), req.masterAwbValue(),
                req.partyKind(), req.partyCode(),
                req.airlineCode(),
                req.portKind(), req.portCode(),
                req.shipmentType(),
                req.teamCode(),
                req.page(), req.size()
        );
    }
}
