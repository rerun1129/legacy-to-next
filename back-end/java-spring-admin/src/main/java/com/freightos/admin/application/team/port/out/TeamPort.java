package com.freightos.admin.application.team.port.out;

import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.domain.team.entity.Team;

import java.util.List;
import java.util.Optional;

public interface TeamPort {
    List<Team> getAllActiveTeams();
    Optional<Team> findTeamByTeamCode(String teamCode);
    List<TeamAutocompleteItem> searchActiveTeams(String query, int limit);
}
