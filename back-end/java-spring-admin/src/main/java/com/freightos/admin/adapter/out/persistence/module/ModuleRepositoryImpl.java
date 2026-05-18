package com.freightos.admin.adapter.out.persistence.module;

import com.freightos.admin.application.module.command.SearchModuleCommand;
import com.freightos.admin.application.module.projection.ModuleSummary;
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
public class ModuleRepositoryImpl implements ModuleRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<ModuleSummary> searchSummaries(SearchModuleCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ModuleJpaEntity> countRoot = countQuery.from(ModuleJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<ModuleJpaEntity> dataQuery = cb.createQuery(ModuleJpaEntity.class);
        Root<ModuleJpaEntity> dataRoot = dataQuery.from(ModuleJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: sortOrder asc, moduleCode asc
        dataQuery.orderBy(cb.asc(dataRoot.get("sortOrder")), cb.asc(dataRoot.get("moduleCode")));

        TypedQuery<ModuleJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<ModuleSummary> content = typedQuery.getResultList().stream()
                .map(e -> new ModuleSummary(e.getId(), e.getModuleCode(), e.getName(), e.getDescription(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ModuleJpaEntity> root, SearchModuleCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.moduleCode())) {
            predicates.add(cb.like(root.get("moduleCode"), command.moduleCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(root.get("name"), "%" + command.name() + "%"));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
