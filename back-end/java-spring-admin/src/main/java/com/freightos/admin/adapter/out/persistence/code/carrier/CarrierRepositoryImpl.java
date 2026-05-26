package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.carrier.entity.CarrierType;
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
public class CarrierRepositoryImpl implements CarrierRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CarrierSummary> searchSummaries(SearchCarrierCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CarrierJpaEntity> countRoot = countQuery.from(CarrierJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<CarrierJpaEntity> dataQuery = cb.createQuery(CarrierJpaEntity.class);
        Root<CarrierJpaEntity> dataRoot = dataQuery.from(CarrierJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // carrier_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("carrierCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<CarrierJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CarrierSummary> content = typedQuery.getResultList().stream()
                .map(e -> new CarrierSummary(e.getId(), e.getCarrierCode(), e.getName(), e.getCarrierType(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CarrierJpaEntity> root, SearchCarrierCommand command) {
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
        if (StringUtils.hasText(command.carrierCode())) {
            predicates.add(cb.like(root.get("carrierCode"), command.carrierCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        if (StringUtils.hasText(command.carrierType())) {
            predicates.add(cb.equal(root.get("carrierType"), CarrierType.valueOf(command.carrierType())));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
