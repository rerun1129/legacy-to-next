package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlDetailResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
import org.springframework.stereotype.Component;

@Component
public class TruckBlAssembler {

    public PagedResult<TruckBlSummaryResponse> toSummaryPage(PagedResult<TruckBlSummary> source) {
        return source.map(TruckBlSummaryResponse::from);
    }

    public SearchTruckBlCommand toSearchCommand(SearchTruckBlRequest req) {
        return new SearchTruckBlCommand(
                req.bound(),
                req.truckBlNo(),
                req.etdFrom(), req.etdTo(),
                req.truckerCode(), req.docPartnerCode(),
                req.partyCode(), req.portCode(),
                req.operatorCode(), req.teamCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    public TruckBlDetailResponse toDetail(TruckBlDetailResult result) {
        return TruckBlDetailResponse.from(result);
    }
}
