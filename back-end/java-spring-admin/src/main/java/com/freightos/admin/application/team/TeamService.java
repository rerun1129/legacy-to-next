package com.freightos.admin.application.team;

import com.freightos.admin.application.team.port.in.TeamUseCase;
import com.freightos.admin.application.team.port.out.TeamPort;
import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.application.team.projection.TeamSummary;
import com.freightos.admin.domain.team.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService implements TeamUseCase {

    private final TeamPort teamPort;

    @Override
    public List<TeamSummary> getAllTeams() {
        List<Team> teams = teamPort.getAllActiveTeams();
        return teams.stream()
                .map(t -> new TeamSummary(t.getId(), t.getTeamCode(), t.getName(), t.getSortOrder(), t.getActive()))
                .toList();
    }

    @Override
    public List<TeamAutocompleteItem> autocompleteTeams(String query, int limit) {
        return teamPort.searchActiveTeams(query, limit);
    }
}
