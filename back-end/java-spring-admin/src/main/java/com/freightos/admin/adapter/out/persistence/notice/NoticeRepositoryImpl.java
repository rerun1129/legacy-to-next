package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.application.notice.command.SearchNoticeCommand;
import com.freightos.admin.application.notice.projection.NoticeSummary;
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
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<NoticeSummary> searchSummaries(SearchNoticeCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<NoticeJpaEntity> countRoot = countQuery.from(NoticeJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<NoticeJpaEntity> dataQuery = cb.createQuery(NoticeJpaEntity.class);
        Root<NoticeJpaEntity> dataRoot = dataQuery.from(NoticeJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // pinned DESC + published_at DESC NULLS LAST + id DESC — T1 flaky 방지 tie-break
        dataQuery.orderBy(
                cb.desc(dataRoot.get("pinned")),
                cb.desc(dataRoot.get("publishedAt")),
                cb.desc(dataRoot.get("id"))
        );

        TypedQuery<NoticeJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<NoticeSummary> content = typedQuery.getResultList().stream()
                .map(e -> new NoticeSummary(e.getId(), e.getTitle(), e.getPinned(), e.getActive(), e.getPublishedAt(), e.getExpiresAt(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<NoticeJpaEntity> root, SearchNoticeCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        applyScope(cb, root, command.scope(), predicates);

        if (command.pinned() != null) {
            predicates.add(cb.equal(root.get("pinned"), command.pinned()));
        }
        if (StringUtils.hasText(command.title())) {
            predicates.add(cb.like(cb.lower(root.get("title")), "%" + command.title().toLowerCase() + "%"));
        }
        if (Boolean.TRUE.equals(command.publishedOnly())) {
            predicates.add(cb.isNotNull(root.get("publishedAt")));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private void applyScope(CriteriaBuilder cb, Root<NoticeJpaEntity> root, String scope, List<Predicate> predicates) {
        // scope 미지정은 ALL로 처리 — deleted 포함 전체 반환
        if (!StringUtils.hasText(scope) || "ALL".equalsIgnoreCase(scope)) {
            return;
        }
        switch (scope.toUpperCase()) {
            case "ACTIVE" -> predicates.add(cb.isNull(root.get("deletedAt")));
            case "INACTIVE" -> {
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.equal(root.get("active"), false));
            }
            case "DELETED" -> predicates.add(cb.isNotNull(root.get("deletedAt")));
            default -> predicates.add(cb.isNull(root.get("deletedAt")));
        }
    }
}
