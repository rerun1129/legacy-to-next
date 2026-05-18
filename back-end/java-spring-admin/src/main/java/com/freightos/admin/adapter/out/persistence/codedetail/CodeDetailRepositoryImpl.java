package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
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
public class CodeDetailRepositoryImpl implements CodeDetailRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CodeDetailSummary> searchSummaries(SearchCodeDetailCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 총 건수 쿼리
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CodeDetailJpaEntity> countRoot = countQuery.from(CodeDetailJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        // 데이터 쿼리
        CriteriaQuery<CodeDetailJpaEntity> dataQuery = cb.createQuery(CodeDetailJpaEntity.class);
        Root<CodeDetailJpaEntity> dataRoot = dataQuery.from(CodeDetailJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: sortOrder asc nulls last, codeValue asc, id asc — T1 flaky 방지
        dataQuery.orderBy(
                cb.asc(cb.selectCase().when(cb.isNull(dataRoot.get("sortOrder")), 1).otherwise(0)),
                cb.asc(dataRoot.get("sortOrder")),
                cb.asc(dataRoot.get("codeValue")),
                cb.asc(dataRoot.get("id"))
        );

        TypedQuery<CodeDetailJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CodeDetailSummary> content = typedQuery.getResultList().stream()
                .map(e -> new CodeDetailSummary(e.getId(), e.getMasterId(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CodeDetailJpaEntity> root, SearchCodeDetailCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (command.masterId() != null) {
            predicates.add(cb.equal(root.get("masterId"), command.masterId()));
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
