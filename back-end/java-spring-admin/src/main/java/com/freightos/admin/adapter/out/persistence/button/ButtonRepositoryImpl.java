package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
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
public class ButtonRepositoryImpl implements ButtonRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<ButtonSummary> searchSummaries(SearchButtonCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ButtonJpaEntity> countRoot = countQuery.from(ButtonJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<ButtonJpaEntity> dataQuery = cb.createQuery(ButtonJpaEntity.class);
        Root<ButtonJpaEntity> dataRoot = dataQuery.from(ButtonJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: menuId asc, sortOrder asc, buttonId asc
        dataQuery.orderBy(cb.asc(dataRoot.get("menuId")), cb.asc(dataRoot.get("sortOrder")), cb.asc(dataRoot.get("id")));

        TypedQuery<ButtonJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<ButtonSummary> content = typedQuery.getResultList().stream()
                .map(e -> new ButtonSummary(e.getId(), e.getButtonCode(), e.getMenuId(), e.getLabel(), e.getActionType(), e.getApiMethod(), e.getApiPath(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<ButtonJpaEntity> root, SearchButtonCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (command.menuId() != null) {
            predicates.add(cb.equal(root.get("menuId"), command.menuId()));
        }
        if (StringUtils.hasText(command.buttonCode())) {
            predicates.add(cb.like(root.get("buttonCode"), command.buttonCode() + "%"));
        }
        if (StringUtils.hasText(command.label())) {
            predicates.add(cb.like(root.get("label"), "%" + command.label() + "%"));
        }
        if (StringUtils.hasText(command.actionType())) {
            predicates.add(cb.equal(root.get("actionType"), command.actionType()));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
