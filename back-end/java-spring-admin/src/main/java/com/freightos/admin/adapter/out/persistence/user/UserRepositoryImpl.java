package com.freightos.admin.adapter.out.persistence.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserScope;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
                .map(e -> new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt(), parseAttributes(e.getAttributes())))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        String sql = """
                SELECT username, COALESCE(user_eng_name, email) AS display_name
                FROM admin.admin_user
                WHERE deleted_at IS NULL
                  AND (username ILIKE :q || '%'
                       OR user_eng_name ILIKE '%' || :q || '%'
                       OR email ILIKE '%' || :q || '%')
                ORDER BY CASE WHEN username ILIKE :q || '%' THEN 0 ELSE 1 END, username
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

    private static Map<String, List<String>> parseAttributes(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            // attributes 역직렬화 실패 시 빈 Map 반환 — 권한 평가에 영향을 주지 않도록
            log.warn("attributes JSON 파싱 실패: {}", ex.getMessage());
            return Map.of();
        }
    }
}
