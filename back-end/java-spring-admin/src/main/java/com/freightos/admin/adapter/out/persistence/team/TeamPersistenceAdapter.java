package com.freightos.admin.adapter.out.persistence.team;

import com.freightos.admin.application.team.port.out.TeamPort;
import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import com.freightos.admin.domain.team.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TeamPersistenceAdapter implements TeamPort {

    private final TeamRepository teamRepository;
    private final TeamJpaToDomainMapper teamJpaToDomainMapper;

    @Override
    public List<Team> getAllActiveTeams() {
        return teamRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(teamJpaToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Team> findTeamByTeamCode(String teamCode) {
        return teamRepository.findByTeamCode(teamCode).map(teamJpaToDomainMapper::toDomain);
    }

    @Override
    public List<TeamAutocompleteItem> searchActiveTeams(String query, int limit) {
        return teamRepository.searchActiveTeams(query, limit);
    }
}
