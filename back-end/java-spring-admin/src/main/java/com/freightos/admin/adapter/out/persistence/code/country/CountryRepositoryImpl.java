package com.freightos.admin.adapter.out.persistence.code.country;

import com.freightos.admin.application.code.country.command.SearchCountryCommand;
import com.freightos.admin.application.code.country.projection.CountrySummary;
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
public class CountryRepositoryImpl implements CountryRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CountrySummary> searchSummaries(SearchCountryCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CountryJpaEntity> countRoot = countQuery.from(CountryJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<CountryJpaEntity> dataQuery = cb.createQuery(CountryJpaEntity.class);
        Root<CountryJpaEntity> dataRoot = dataQuery.from(CountryJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // country_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("countryCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<CountryJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CountrySummary> content = typedQuery.getResultList().stream()
                .map(e -> new CountrySummary(e.getId(), e.getCountryCode(), e.getName(), e.getNameEn(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CountryJpaEntity> root, SearchCountryCommand command) {
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
        if (StringUtils.hasText(command.countryCode())) {
            predicates.add(cb.like(root.get("countryCode"), command.countryCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
