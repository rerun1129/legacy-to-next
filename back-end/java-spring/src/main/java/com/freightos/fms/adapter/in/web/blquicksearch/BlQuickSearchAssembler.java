package com.freightos.fms.adapter.in.web.blquicksearch;

import com.freightos.fms.adapter.in.web.blquicksearch.dto.BlQuickSearchAutocompleteRequest;
import com.freightos.fms.adapter.in.web.blquicksearch.dto.BlQuickSearchItemResponse;
import com.freightos.fms.application.blquicksearch.command.BlQuickSearchCommand;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlQuickSearchAssembler {

    public BlQuickSearchCommand toCommand(BlQuickSearchAutocompleteRequest req) {
        return new BlQuickSearchCommand(
                req.q(),
                req.jobDiv(),
                req.bound(),
                req.dateKind(),
                req.dateFrom(),
                req.dateTo(),
                req.teamCode(),
                req.operatorCode(),
                req.salesManCode(),
                req.polCode(),
                req.podCode(),
                req.partyKind(),
                req.partyCode(),
                req.limit()
        );
    }

    public List<BlQuickSearchItemResponse> toResponseList(List<BlQuickSearchSummary> summaries) {
        return summaries.stream()
                .map(BlQuickSearchItemResponse::from)
                .toList();
    }
}
