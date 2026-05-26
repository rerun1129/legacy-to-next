package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
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
public class ExchangeRateRepositoryImpl implements ExchangeRateRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<ExchangeRateSummary> searchSummaries(SearchExchangeRateCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ExchangeRateJpaEntity> countRoot = countQuery.from(ExchangeRateJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<ExchangeRateJpaEntity> dataQuery = cb.createQuery(ExchangeRateJpaEntity.class);
        Root<ExchangeRateJpaEntity> dataRoot = dataQuery.from(ExchangeRateJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // base_currency asc + target_currency asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("baseCurrency")), cb.asc(dataRoot.get("targetCurrency")), cb.asc(dataRoot.get("id")));

        TypedQuery<ExchangeRateJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<ExchangeRateSummary> content = typedQuery.getResultList().stream()
                .map(e -> new ExchangeRateSummary(e.getId(), e.getBaseCurrency(), e.getTargetCurrency(), e.getRate(), e.getName(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ExchangeRateJpaEntity> root, SearchExchangeRateCommand command) {
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
        if (StringUtils.hasText(command.baseCurrency())) {
            predicates.add(cb.equal(root.get("baseCurrency"), command.baseCurrency()));
        }
        if (StringUtils.hasText(command.targetCurrency())) {
            predicates.add(cb.equal(root.get("targetCurrency"), command.targetCurrency()));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
