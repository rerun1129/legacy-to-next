package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
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
public class AttributeValueRepositoryImpl implements AttributeValueRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<AttributeValueSummary> searchSummaries(SearchAttributeValueCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AttributeValueJpaEntity> countRoot = countQuery.from(AttributeValueJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<AttributeValueJpaEntity> dataQuery = cb.createQuery(AttributeValueJpaEntity.class);
        Root<AttributeValueJpaEntity> dataRoot = dataQuery.from(AttributeValueJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: attributeKey asc, sortOrder asc, value asc
        dataQuery.orderBy(
                cb.asc(dataRoot.get("id").get("attributeKey")),
                cb.asc(dataRoot.get("sortOrder")),
                cb.asc(dataRoot.get("id").get("value"))
        );

        TypedQuery<AttributeValueJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<AttributeValueSummary> content = typedQuery.getResultList().stream()
                .map(e -> new AttributeValueSummary(e.getId().getAttributeKey(), e.getId().getValue(), e.getLabel(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<AttributeValueJpaEntity> root, SearchAttributeValueCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.attributeKey())) {
            predicates.add(cb.equal(root.get("id").get("attributeKey"), command.attributeKey()));
        }
        if (StringUtils.hasText(command.value())) {
            predicates.add(cb.like(root.get("id").get("value"), "%" + command.value() + "%"));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
