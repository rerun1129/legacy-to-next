package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
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
public class AttributeDefinitionRepositoryImpl implements AttributeDefinitionRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<AttributeDefinitionSummary> searchSummaries(SearchAttributeDefinitionCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AttributeDefinitionJpaEntity> countRoot = countQuery.from(AttributeDefinitionJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<AttributeDefinitionJpaEntity> dataQuery = cb.createQuery(AttributeDefinitionJpaEntity.class);
        Root<AttributeDefinitionJpaEntity> dataRoot = dataQuery.from(AttributeDefinitionJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: attributeKey asc
        dataQuery.orderBy(cb.asc(dataRoot.get("attributeKey")));

        TypedQuery<AttributeDefinitionJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<AttributeDefinitionSummary> content = typedQuery.getResultList().stream()
                .map(e -> new AttributeDefinitionSummary(e.getId(), e.getAttributeKey(), e.getName(), e.getDescription(), e.getValueType(), e.getActive(), e.getAllowMulti(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<AttributeDefinitionJpaEntity> root, SearchAttributeDefinitionCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.attributeKey())) {
            predicates.add(cb.like(root.get("attributeKey"), command.attributeKey() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(root.get("name"), "%" + command.name() + "%"));
        }
        if (StringUtils.hasText(command.valueType())) {
            predicates.add(cb.equal(root.get("valueType"), command.valueType()));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
