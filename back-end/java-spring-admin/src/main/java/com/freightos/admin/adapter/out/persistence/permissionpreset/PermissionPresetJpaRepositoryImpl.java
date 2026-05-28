package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.common.response.AutocompleteItem;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PermissionPresetJpaRepositoryImpl implements PermissionPresetRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<AutocompleteItem> autocompletePermissionPresets(String query, int limit) {
        String sql = """
                SELECT code, name FROM admin.permission_preset
                WHERE active = true
                  AND (code ILIKE '%' || :q || '%' OR name ILIKE '%' || :q || '%')
                ORDER BY code
                LIMIT :limit
                """;
        List<?> rows = em.createNativeQuery(sql)
                .setParameter("q", query)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> { Object[] cols = (Object[]) row; return new AutocompleteItem((String) cols[0], (String) cols[1]); })
                .toList();
    }
}
