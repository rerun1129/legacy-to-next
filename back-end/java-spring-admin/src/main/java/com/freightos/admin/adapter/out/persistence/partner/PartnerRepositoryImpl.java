package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.partner.entity.PartnerType;
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
public class PartnerRepositoryImpl implements PartnerRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<PartnerSummary> searchSummaries(SearchPartnerCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PartnerJpaEntity> countRoot = countQuery.from(PartnerJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<PartnerJpaEntity> dataQuery = cb.createQuery(PartnerJpaEntity.class);
        Root<PartnerJpaEntity> dataRoot = dataQuery.from(PartnerJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // partner_code asc + id asc tie-break — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("partnerCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<PartnerJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<PartnerSummary> content = typedQuery.getResultList().stream()
                .map(e -> new PartnerSummary(e.getId(), e.getPartnerCode(), e.getPartnerType(), e.getName(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<PartnerJpaEntity> root, SearchPartnerCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        if (!command.includeDeleted()) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        if (StringUtils.hasText(command.partnerCode())) {
            predicates.add(cb.like(root.get("partnerCode"), command.partnerCode() + "%"));
        }
        if (StringUtils.hasText(command.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + command.name().toLowerCase() + "%"));
        }
        if (StringUtils.hasText(command.partnerType())) {
            predicates.add(cb.equal(root.get("partnerType"), PartnerType.valueOf(command.partnerType())));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
