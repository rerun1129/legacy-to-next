package com.freightos.fms.adapter.in.web.seahouse;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.seahouse.dto.SearchSeaHouseRequest;
import com.freightos.fms.adapter.in.web.seahouse.dto.SeaHouseSummaryResponse;
import com.freightos.fms.application.seahouse.command.SearchSeaHouseCommand;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
import org.springframework.stereotype.Component;

@Component
public class SeaHouseAssembler {

    public PagedResult<SeaHouseSummaryResponse> toSummaryPage(PagedResult<SeaHouseSummary> source) {
        return source.map(SeaHouseSummaryResponse::from);
    }

    public SearchSeaHouseCommand toSearchCommand(SearchSeaHouseRequest req) {
        return new SearchSeaHouseCommand(
                req.bound(),
                req.dateKind(),
                req.dateFrom(), req.dateTo(),
                req.masterBlKind(), req.masterBlValue(),
                req.hblNo(),
                req.partyKind(), req.partyCode(),
                req.actualCustomerCode(),
                req.partnerKind(), req.partnerCode(),
                req.linerCode(),
                req.portKind(), req.portCode(),
                req.shipmentType(),
                req.teamCode(), req.operatorCode(),
                req.salesClass(), req.salesManCode(),
                req.incoterms(),
                req.vesselName(), req.voyageNo(),
                req.loadType(),
                req.page(), req.size()
        );
    }
}
