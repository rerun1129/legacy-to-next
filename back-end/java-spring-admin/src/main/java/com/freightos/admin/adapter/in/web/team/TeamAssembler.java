package com.freightos.admin.adapter.in.web.team;

import com.freightos.admin.adapter.in.web.team.dto.TeamAutocompleteResponse;
import com.freightos.admin.adapter.in.web.team.dto.TeamSummaryResponse;
import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.application.team.projection.TeamSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeamAssembler {

    public TeamSummaryResponse toSummaryResponse(TeamSummary summary) {
        return new TeamSummaryResponse(summary.id(), summary.teamCode(), summary.name(), summary.sortOrder(), summary.active());
    }

    public List<TeamSummaryResponse> toSummaryResponseList(List<TeamSummary> summaries) {
        return summaries.stream().map(this::toSummaryResponse).toList();
    }

    public TeamAutocompleteResponse toAutocompleteResponse(TeamAutocompleteItem item) {
        return new TeamAutocompleteResponse(item.id(), item.code(), item.name());
    }

    public List<TeamAutocompleteResponse> toAutocompleteResponseList(List<TeamAutocompleteItem> items) {
        return items.stream().map(this::toAutocompleteResponse).toList();
    }
}
