package com.freightos.admin.adapter.out.persistence.team;

import com.freightos.admin.application.team.projection.TeamAutocompleteItem;

import java.util.List;

public interface TeamRepositoryCustom {
    List<TeamAutocompleteItem> searchActiveTeams(String query, int limit);
}
