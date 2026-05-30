package com.freightos.admin.adapter.out.persistence.team;

import com.freightos.admin.application.team.projection.TeamSummary;
import com.freightos.admin.domain.team.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamJpaToDomainMapper {

    public Team toDomain(TeamJpaEntity e) {
        Team domain = Team.create(e.getTeamCode(), e.getName(), e.getDescription(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    public TeamSummary toSummary(TeamJpaEntity e) {
        return new TeamSummary(e.getId(), e.getTeamCode(), e.getName(), e.getSortOrder(), e.getActive());
    }
}
