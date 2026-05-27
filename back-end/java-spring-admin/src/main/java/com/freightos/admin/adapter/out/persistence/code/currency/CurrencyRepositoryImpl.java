package com.freightos.admin.adapter.out.persistence.code.currency;

import com.freightos.admin.application.code.currency.command.SearchCurrencyCommand;
import com.freightos.admin.application.code.currency.projection.CurrencySummary;
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
public class CurrencyRepositoryImpl implements CurrencyRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CurrencySummary> searchSummaries(SearchCurrencyCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CurrencyJpaEntity> countRoot = countQuery.from(CurrencyJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<CurrencyJpaEntity> dataQuery = cb.createQuery(CurrencyJpaEntity.class);
        Root<CurrencyJpaEntity> dataRoot = dataQuery.from(CurrencyJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // currency_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("currencyCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<CurrencyJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CurrencySummary> content = typedQuery.getResultList().stream()
                .map(e -> new CurrencySummary(e.getId(), e.getCurrencyCode(), e.getName(), e.getNameEn(), e.getSymbol(), e.getCurrencyUnit(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        String sql = """
                SELECT currency_code, name FROM admin.currency
                WHERE deleted_at IS NULL
                  AND (currency_code ILIKE :q || '%' OR name ILIKE '%' || :q || '%')
                ORDER BY currency_code
                LIMIT :limit
                """;
        // JPA 2.x createNativeQuery(sql) returns raw Query; Object[] cast is the standard pattern
        List<?> rows = em.createNativeQuery(sql)
                .setParameter("q", query)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> { Object[] cols = (Object[]) row; return new AutocompleteItem((String) cols[0], (String) cols[1]); })
                .toList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CurrencyJpaEntity> root, SearchCurrencyCommand command) {
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
        if (StringUtils.hasText(command.currencyCode())) {
            predicates.add(cb.like(root.get("currencyCode"), command.currencyCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
