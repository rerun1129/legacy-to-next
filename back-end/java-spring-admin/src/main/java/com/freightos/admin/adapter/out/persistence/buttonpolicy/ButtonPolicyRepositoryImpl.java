package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
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
public class ButtonPolicyRepositoryImpl implements ButtonPolicyRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<ButtonPolicySummary> searchSummaries(SearchButtonPolicyCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ButtonPolicyJpaEntity> countRoot = countQuery.from(ButtonPolicyJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<ButtonPolicyJpaEntity> dataQuery = cb.createQuery(ButtonPolicyJpaEntity.class);
        Root<ButtonPolicyJpaEntity> dataRoot = dataQuery.from(ButtonPolicyJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: buttonId asc, attributeKey asc, requiredValue asc, policyId asc
        dataQuery.orderBy(cb.asc(dataRoot.get("buttonId")), cb.asc(dataRoot.get("attributeKey")), cb.asc(dataRoot.get("requiredValue")), cb.asc(dataRoot.get("id")));

        TypedQuery<ButtonPolicyJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<ButtonPolicySummary> content = typedQuery.getResultList().stream()
                .map(e -> new ButtonPolicySummary(e.getId(), e.getButtonId(), e.getAttributeKey(), e.getRequiredValue(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ButtonPolicyJpaEntity> root, SearchButtonPolicyCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (command.buttonId() != null) {
            predicates.add(cb.equal(root.get("buttonId"), command.buttonId()));
        }
        if (StringUtils.hasText(command.attributeKey())) {
            predicates.add(cb.equal(root.get("attributeKey"), command.attributeKey()));
        }
        if (StringUtils.hasText(command.requiredValue())) {
            predicates.add(cb.equal(root.get("requiredValue"), command.requiredValue()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
