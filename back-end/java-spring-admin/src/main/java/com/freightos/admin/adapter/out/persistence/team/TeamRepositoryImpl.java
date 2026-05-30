package com.freightos.admin.adapter.out.persistence.team;

import com.freightos.admin.application.team.projection.TeamAutocompleteItem;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class TeamRepositoryImpl implements TeamRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<TeamAutocompleteItem> searchActiveTeams(String query, int limit) {
        // 코드 prefix 우선 정렬 후 sort_order — "fix(admin): currency·hsCode autocomplete 코드 prefix 우선 정렬" 동일 패턴
        String sql = """
                SELECT team_id, team_code, name FROM admin.team
                WHERE active = true
                  AND (team_code ILIKE :q || '%' OR name ILIKE '%' || :q || '%')
                ORDER BY CASE WHEN team_code ILIKE :q || '%' THEN 0 ELSE 1 END, sort_order, team_code
                LIMIT :limit
                """;
        List<?> rows = em.createNativeQuery(sql)
                .setParameter("q", query)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> {
                    Object[] cols = (Object[]) row;
                    return new TeamAutocompleteItem(((Number) cols[0]).longValue(), (String) cols[1], (String) cols[2]);
                })
                .toList();
    }
}
