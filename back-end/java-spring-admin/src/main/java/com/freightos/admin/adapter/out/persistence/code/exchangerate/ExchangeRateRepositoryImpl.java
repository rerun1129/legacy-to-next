package com.freightos.admin.adapter.out.persistence.code.exchangerate;

import com.freightos.admin.application.code.exchangerate.command.SearchExchangeRateCommand;
import com.freightos.admin.application.code.exchangerate.projection.ExchangeRateSummary;
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
        // from_currency_code asc + to_currency_code asc + exchange_date asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(
                cb.asc(dataRoot.get("fromCurrencyCode")),
                cb.asc(dataRoot.get("toCurrencyCode")),
                cb.asc(dataRoot.get("exchangeDate")),
                cb.asc(dataRoot.get("id"))
        );

        TypedQuery<ExchangeRateJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<ExchangeRateSummary> content = typedQuery.getResultList().stream()
                .map(e -> new ExchangeRateSummary(e.getId(), e.getFromCurrencyCode(), e.getToCurrencyCode(), e.getExchangeDate(), e.getCashSellExchangeRate(), e.getCashBuyExchangeRate(), e.getWireSendExchangeRate(), e.getWireReceiveExchangeRate(), e.getStandardExchangeRate(), e.getName(), e.getNameEn(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        String sql = """
                SELECT from_currency_code || '/' || to_currency_code AS code, name FROM admin.exchange_rate
                WHERE deleted_at IS NULL
                  AND (from_currency_code ILIKE :q || '%' OR to_currency_code ILIKE :q || '%' OR name ILIKE '%' || :q || '%')
                ORDER BY from_currency_code, to_currency_code
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
        if (StringUtils.hasText(command.fromCurrencyCode())) {
            predicates.add(cb.equal(root.get("fromCurrencyCode"), command.fromCurrencyCode()));
        }
        if (StringUtils.hasText(command.toCurrencyCode())) {
            predicates.add(cb.equal(root.get("toCurrencyCode"), command.toCurrencyCode()));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
