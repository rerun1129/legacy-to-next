package com.freightos.admin.adapter.out.persistence.faqcategory;

import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
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
public class FaqCategoryRepositoryImpl implements FaqCategoryRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<FaqCategorySummary> searchSummaries(SearchFaqCategoryCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FaqCategoryJpaEntity> countRoot = countQuery.from(FaqCategoryJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<FaqCategoryJpaEntity> dataQuery = cb.createQuery(FaqCategoryJpaEntity.class);
        Root<FaqCategoryJpaEntity> dataRoot = dataQuery.from(FaqCategoryJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // sort_order ASC, id ASC — T1 flaky 방지 tie-break
        dataQuery.orderBy(cb.asc(dataRoot.get("sortOrder")), cb.asc(dataRoot.get("id")));

        TypedQuery<FaqCategoryJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<FaqCategorySummary> content = typedQuery.getResultList().stream()
                .map(e -> new FaqCategorySummary(e.getId(), e.getName(), e.getSortOrder(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public boolean existsByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<FaqCategoryJpaEntity> root = query.from(FaqCategoryJpaEntity.class);
        query.select(cb.count(root)).where(cb.equal(root.get("name"), name));
        return em.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<FaqCategoryJpaEntity> root = query.from(FaqCategoryJpaEntity.class);
        query.select(cb.count(root))
                .where(cb.equal(root.get("name"), name), cb.notEqual(root.get("id"), id));
        return em.createQuery(query).getSingleResult() > 0;
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<FaqCategoryJpaEntity> root, SearchFaqCategoryCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        applyScope(cb, root, command.scope(), predicates);

        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private void applyScope(CriteriaBuilder cb, Root<FaqCategoryJpaEntity> root, String scope, List<Predicate> predicates) {
        if (!StringUtils.hasText(scope) || "ALL".equalsIgnoreCase(scope)) {
            return;
        }
        switch (scope.toUpperCase()) {
            case "ACTIVE"  -> predicates.add(cb.isNull(root.get("deletedAt")));
            case "DELETED" -> predicates.add(cb.isNotNull(root.get("deletedAt")));
            default        -> predicates.add(cb.isNull(root.get("deletedAt")));
        }
    }
}
