package com.freightos.admin.adapter.out.persistence.menu;

import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<MenuSummary> searchSummaries(SearchMenuCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MenuJpaEntity> countRoot = countQuery.from(MenuJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<MenuJpaEntity> dataQuery = cb.createQuery(MenuJpaEntity.class);
        Root<MenuJpaEntity> dataRoot = dataQuery.from(MenuJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: moduleCode asc, sortOrder asc, menuId asc
        dataQuery.orderBy(cb.asc(dataRoot.get("moduleCode")), cb.asc(dataRoot.get("sortOrder")), cb.asc(dataRoot.get("id")));

        TypedQuery<MenuJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<MenuSummary> content = typedQuery.getResultList().stream()
                .map(e -> new MenuSummary(e.getId(), e.getMenuCode(), e.getParentId(), e.getPath(), e.getLabel(), e.getLabelEn(), e.getIcon(), e.getSortOrder(), e.getActive(), e.getModuleCode(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocompleteMenuCodes(String query, int limit) {
        String sql = """
                SELECT menu_code, label FROM admin.menu
                WHERE active = true
                  AND (menu_code ILIKE '%' || :q || '%' OR label ILIKE '%' || :q || '%')
                ORDER BY menu_code
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

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<MenuJpaEntity> root, SearchMenuCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.menuCode())) {
            predicates.add(cb.like(root.get("menuCode"), command.menuCode() + "%"));
        }
        if (StringUtils.hasText(command.label())) {
            predicates.add(cb.like(root.get("label"), "%" + command.label() + "%"));
        }
        if (StringUtils.hasText(command.moduleCode())) {
            predicates.add(cb.equal(root.get("moduleCode"), command.moduleCode()));
        }
        if (command.parentId() != null) {
            predicates.add(cb.equal(root.get("parentId"), command.parentId()));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
