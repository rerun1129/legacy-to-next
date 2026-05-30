package com.freightos.admin.application.team.port.in;

import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.application.team.projection.TeamSummary;

import java.util.List;

public interface TeamUseCase {
    List<TeamSummary> getAllTeams();
    List<TeamAutocompleteItem> autocompleteTeams(String query, int limit);
}
