package com.freightos.admin.adapter.out.persistence.subscriber;

import com.freightos.admin.application.subscriber.command.SearchSubscriberCommand;
import com.freightos.admin.application.subscriber.projection.SubscriberSummary;
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
public class SubscriberRepositoryImpl implements SubscriberRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<SubscriberSummary> searchSummaries(SearchSubscriberCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SubscriberJpaEntity> countRoot = countQuery.from(SubscriberJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<SubscriberJpaEntity> dataQuery = cb.createQuery(SubscriberJpaEntity.class);
        Root<SubscriberJpaEntity> dataRoot = dataQuery.from(SubscriberJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // subscriber_code asc + subscriberId asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("subscriberCode")), cb.asc(dataRoot.get("subscriberId")));

        TypedQuery<SubscriberJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<SubscriberSummary> content = typedQuery.getResultList().stream()
                .map(e -> new SubscriberSummary(e.getSubscriberId(), e.getSubscriberCode(), e.getName(),
                        e.getNameEn(), e.getBusinessNo(), e.getRepresentative(), e.getPhone(),
                        e.getEmail(), e.getMemo(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        String sql = """
                SELECT subscriber_code, name FROM admin.subscriber
                WHERE deleted_at IS NULL
                  AND (subscriber_code ILIKE :q || '%' OR name ILIKE '%' || :q || '%')
                ORDER BY CASE WHEN subscriber_code ILIKE :q || '%' THEN 0 ELSE 1 END, subscriber_code
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

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<SubscriberJpaEntity> root, SearchSubscriberCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        String scope = (command.scope() == null || command.scope().isBlank()) ? "ALL" : command.scope();
        switch (scope) {
            case "ACTIVE":
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.isTrue(root.get("active")));
                break;
            case "INACTIVE":
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.isFalse(root.get("active")));
                break;
            case "DELETED":
                predicates.add(cb.isNotNull(root.get("deletedAt")));
                break;
            case "ALL":
            default:
                predicates.add(cb.isNull(root.get("deletedAt")));
                break;
        }
        if (StringUtils.hasText(command.subscriberCode())) {
            predicates.add(cb.like(root.get("subscriberCode"), command.subscriberCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
