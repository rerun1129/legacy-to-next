package com.freightos.fms.adapter.in.web.airhouse;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.airhouse.dto.SearchAirHouseRequest;
import com.freightos.fms.adapter.in.web.airhouse.dto.AirHouseSummaryResponse;
import com.freightos.fms.application.airhouse.command.SearchAirHouseCommand;
import com.freightos.fms.application.airhouse.projection.AirHouseListItem;
import org.springframework.stereotype.Component;

@Component
public class AirHouseAssembler {

    public PagedResult<AirHouseSummaryResponse> toSummaryPage(PagedResult<AirHouseListItem> source) {
        return source.map(AirHouseSummaryResponse::from);
    }

    public SearchAirHouseCommand toSearchCommand(SearchAirHouseRequest req) {
        return new SearchAirHouseCommand(
                req.bound(),
                req.dateKind(),
                req.dateFrom(), req.dateTo(),
                req.masterAwbKind(), req.masterAwbValue(),
                req.hblNo(),
                req.partyKind(), req.partyCode(),
                req.actualCustomerCode(), req.settlePartnerCode(),
                req.airlineCode(),
                req.portKind(), req.portCode(),
                req.shipmentType(),
                req.teamCode(), req.operatorCode(),
                req.salesClass(), req.salesManCode(),
                req.incoterms(),
                req.page(), req.size()
        );
    }
}
