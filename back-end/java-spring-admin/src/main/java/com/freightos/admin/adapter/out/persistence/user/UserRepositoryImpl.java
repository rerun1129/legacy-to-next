package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserScope;
import com.freightos.admin.application.user.projection.UserSummary;
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
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<UserSummary> searchSummaries(SearchUserCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UserJpaEntity> countRoot = countQuery.from(UserJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        CriteriaQuery<UserJpaEntity> dataQuery = cb.createQuery(UserJpaEntity.class);
        Root<UserJpaEntity> dataRoot = dataQuery.from(UserJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: username asc, id asc — T1 flaky 방지 (username UNIQUE이므로 id는 보조)
        dataQuery.orderBy(cb.asc(dataRoot.get("username")), cb.asc(dataRoot.get("id")));

        TypedQuery<UserJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<UserSummary> content = typedQuery.getResultList().stream()
                .map(e -> new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<UserJpaEntity> root, SearchUserCommand command) {
        List<Predicate> predicates = new ArrayList<>();

        UserScope scope = command.scope() != null ? command.scope() : UserScope.ALL;
        switch (scope) {
            case ALL:
                predicates.add(cb.isNull(root.get("deletedAt")));
                break;
            case ACTIVE:
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.isTrue(root.get("active")));
                break;
            case INACTIVE:
                predicates.add(cb.isNull(root.get("deletedAt")));
                predicates.add(cb.isFalse(root.get("active")));
                break;
            case DELETED:
                predicates.add(cb.isNotNull(root.get("deletedAt")));
                break;
        }

        if (StringUtils.hasText(command.username())) {
            predicates.add(cb.like(root.get("username"), command.username() + "%"));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
