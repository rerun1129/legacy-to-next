package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
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
public class MenuPolicyRepositoryImpl implements MenuPolicyRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<MenuPolicySummary> searchSummaries(SearchMenuPolicyCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MenuPolicyJpaEntity> countRoot = countQuery.from(MenuPolicyJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<MenuPolicyJpaEntity> dataQuery = cb.createQuery(MenuPolicyJpaEntity.class);
        Root<MenuPolicyJpaEntity> dataRoot = dataQuery.from(MenuPolicyJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: menuId asc, attributeKey asc, requiredValue asc, policyId asc
        dataQuery.orderBy(cb.asc(dataRoot.get("menuId")), cb.asc(dataRoot.get("attributeKey")), cb.asc(dataRoot.get("requiredValue")), cb.asc(dataRoot.get("id")));

        TypedQuery<MenuPolicyJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<MenuPolicySummary> content = typedQuery.getResultList().stream()
                .map(e -> new MenuPolicySummary(e.getId(), e.getMenuId(), e.getAttributeKey(), e.getRequiredValue(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<MenuPolicyJpaEntity> root, SearchMenuPolicyCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (command.menuId() != null) {
            predicates.add(cb.equal(root.get("menuId"), command.menuId()));
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
