package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.port.entity.PortType;
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
public class PortRepositoryImpl implements PortRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<PortSummary> searchSummaries(SearchPortCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PortJpaEntity> countRoot = countQuery.from(PortJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<PortJpaEntity> dataQuery = cb.createQuery(PortJpaEntity.class);
        Root<PortJpaEntity> dataRoot = dataQuery.from(PortJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // port_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("portCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<PortJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<PortSummary> content = typedQuery.getResultList().stream()
                .map(e -> new PortSummary(e.getId(), e.getPortCode(), e.getName(), e.getNameEn(), e.getCountryCode(), e.getPortType(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, String type, int limit) {
        String sql = """
                SELECT port_code, name FROM admin.port
                WHERE deleted_at IS NULL
                  AND (port_code ILIKE :q || '%' OR name ILIKE '%' || :q || '%')
                  AND (CAST(:type AS text) IS NULL OR port_type = CAST(:type AS text))
                ORDER BY CASE WHEN port_code ILIKE :q || '%' THEN 0 ELSE 1 END, port_code
                LIMIT :limit
                """;
        // JPA 2.x createNativeQuery(sql) returns raw Query; Object[] cast is the standard pattern
        List<?> rows = em.createNativeQuery(sql)
                .setParameter("q", query)
                .setParameter("type", type)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> { Object[] cols = (Object[]) row; return new AutocompleteItem((String) cols[0], (String) cols[1]); })
                .toList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<PortJpaEntity> root, SearchPortCommand command) {
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
        if (StringUtils.hasText(command.portCode())) {
            predicates.add(cb.like(root.get("portCode"), command.portCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        if (StringUtils.hasText(command.countryCode())) {
            predicates.add(cb.equal(root.get("countryCode"), command.countryCode()));
        }
        if (StringUtils.hasText(command.portType())) {
            predicates.add(cb.equal(root.get("portType"), PortType.valueOf(command.portType())));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
