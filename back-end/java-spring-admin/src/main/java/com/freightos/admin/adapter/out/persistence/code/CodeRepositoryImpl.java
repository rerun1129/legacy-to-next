package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
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
public class CodeRepositoryImpl implements CodeRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CodeSummary> searchSummaries(SearchCodeCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 총 건수 쿼리
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CodeJpaEntity> countRoot = countQuery.from(CodeJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        // 데이터 쿼리
        CriteriaQuery<CodeJpaEntity> dataQuery = cb.createQuery(CodeJpaEntity.class);
        Root<CodeJpaEntity> dataRoot = dataQuery.from(CodeJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: codeGroup asc, codeValue asc, id asc — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("codeGroup")), cb.asc(dataRoot.get("codeValue")), cb.asc(dataRoot.get("id")));

        TypedQuery<CodeJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CodeSummary> content = typedQuery.getResultList().stream()
                .map(e -> new CodeSummary(e.getId(), e.getCodeGroup(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CodeJpaEntity> root, SearchCodeCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.codeGroup())) {
            predicates.add(cb.like(root.get("codeGroup"), command.codeGroup() + "%"));
        }
        if (StringUtils.hasText(command.codeValue())) {
            predicates.add(cb.like(root.get("codeValue"), command.codeValue() + "%"));
        }
        if (StringUtils.hasText(command.codeLabel())) {
            predicates.add(cb.like(root.get("codeLabel"), "%" + command.codeLabel() + "%"));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
