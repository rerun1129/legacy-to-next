package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
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
public class FaqRepositoryImpl implements FaqRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<FaqSummary> searchSummaries(SearchFaqCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FaqJpaEntity> countRoot = countQuery.from(FaqJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<FaqJpaEntity> dataQuery = cb.createQuery(FaqJpaEntity.class);
        Root<FaqJpaEntity> dataRoot = dataQuery.from(FaqJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // sort_order ASC, id ASC — T1 flaky 방지 tie-break
        dataQuery.orderBy(cb.asc(dataRoot.get("sortOrder")), cb.asc(dataRoot.get("id")));

        TypedQuery<FaqJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<FaqSummary> content = typedQuery.getResultList().stream()
                .map(e -> new FaqSummary(e.getId(), e.getFaqCategoryId(), e.getQuestion(), e.getSortOrder(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<FaqJpaEntity> root, SearchFaqCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        applyScope(cb, root, command.scope(), predicates);

        if (command.faqCategoryId() != null) {
            predicates.add(cb.equal(root.get("faqCategoryId"), command.faqCategoryId()));
        }
        if (StringUtils.hasText(command.question())) {
            predicates.add(cb.like(cb.lower(root.get("question")), "%" + command.question().toLowerCase() + "%"));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private void applyScope(CriteriaBuilder cb, Root<FaqJpaEntity> root, String scope, List<Predicate> predicates) {
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
