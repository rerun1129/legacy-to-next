package com.freightos.admin.adapter.out.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<TeamJpaEntity, Long>, TeamRepositoryCustom {
    List<TeamJpaEntity> findByActiveTrueOrderBySortOrderAsc();
    Optional<TeamJpaEntity> findByTeamCode(String teamCode);
}
