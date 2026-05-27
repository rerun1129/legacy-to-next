package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
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
public class FreightRepositoryImpl implements FreightRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<FreightSummary> searchSummaries(SearchFreightCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FreightJpaEntity> countRoot = countQuery.from(FreightJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<FreightJpaEntity> dataQuery = cb.createQuery(FreightJpaEntity.class);
        Root<FreightJpaEntity> dataRoot = dataQuery.from(FreightJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // freight_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("freightCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<FreightJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<FreightSummary> content = typedQuery.getResultList().stream()
                .map(e -> new FreightSummary(e.getId(), e.getFreightCode(), e.getName(), e.getNameEn(), e.getDescription(), e.getFreightUnit(), e.getFreightGroup(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<FreightJpaEntity> root, SearchFreightCommand command) {
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
        if (StringUtils.hasText(command.freightCode())) {
            predicates.add(cb.like(root.get("freightCode"), command.freightCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
