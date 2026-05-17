package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.TermsType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TermsRepositoryImpl implements TermsRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<TermsSummary> searchSummaries(SearchTermsCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<TermsJpaEntity> countRoot = countQuery.from(TermsJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<TermsJpaEntity> dataQuery = cb.createQuery(TermsJpaEntity.class);
        Root<TermsJpaEntity> dataRoot = dataQuery.from(TermsJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // type ASC, version DESC — T1 flaky 방지 tie-break 역할도 겸함
        dataQuery.orderBy(cb.asc(dataRoot.get("type")), cb.desc(dataRoot.get("version")));

        TypedQuery<TermsJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<TermsSummary> content = typedQuery.getResultList().stream()
                .map(e -> new TermsSummary(e.getId(), e.getType().name(), e.getVersion(), e.getEffectiveAt(), e.getSummary(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public boolean existsByTypeAndVersion(TermsType type, int version) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<TermsJpaEntity> root = query.from(TermsJpaEntity.class);
        query.select(cb.count(root))
                .where(cb.equal(root.get("type"), type), cb.equal(root.get("version"), version));
        return em.createQuery(query).getSingleResult() > 0;
    }

    @Override
    public Optional<TermsJpaEntity> findActiveByType(TermsType type, LocalDateTime asOf) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TermsJpaEntity> query = cb.createQuery(TermsJpaEntity.class);
        Root<TermsJpaEntity> root = query.from(TermsJpaEntity.class);

        query.where(
                cb.equal(root.get("type"), type),
                cb.isNull(root.get("deletedAt")),
                cb.lessThanOrEqualTo(root.get("effectiveAt"), asOf)
        );
        // effective_at 최대 1건 — version DESC를 secondary key로 사용 (T1 tie-break)
        query.orderBy(cb.desc(root.get("effectiveAt")), cb.desc(root.get("version")));

        List<TermsJpaEntity> results = em.createQuery(query).setMaxResults(1).getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<TermsJpaEntity> root, SearchTermsCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        applyScope(cb, root, command.scope(), predicates);

        if (StringUtils.hasText(command.type())) {
            predicates.add(cb.equal(root.get("type"), TermsType.valueOf(command.type())));
        }
        if (command.version() != null) {
            predicates.add(cb.equal(root.get("version"), command.version()));
        }
        if (StringUtils.hasText(command.summary())) {
            predicates.add(cb.like(cb.lower(root.get("summary")), "%" + command.summary().toLowerCase() + "%"));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private void applyScope(CriteriaBuilder cb, Root<TermsJpaEntity> root, String scope, List<Predicate> predicates) {
        // scope 미지정은 ALL로 처리 — deleted 포함 전체 반환
        if (!StringUtils.hasText(scope) || "ALL".equalsIgnoreCase(scope)) {
            return;
        }
        switch (scope.toUpperCase()) {
            case "ACTIVE" -> predicates.add(cb.isNull(root.get("deletedAt")));
            case "DELETED" -> predicates.add(cb.isNotNull(root.get("deletedAt")));
            default -> predicates.add(cb.isNull(root.get("deletedAt")));
        }
    }
}
