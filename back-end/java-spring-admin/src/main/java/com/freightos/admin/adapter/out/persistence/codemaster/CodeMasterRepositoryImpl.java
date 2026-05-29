package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.AutocompleteItem;
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
public class CodeMasterRepositoryImpl implements CodeMasterRepositoryCustom {

    private final EntityManager em;

    @Override
    public PagedResult<CodeMasterSummary> searchSummaries(SearchCodeMasterCommand command) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // 총 건수 쿼리
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CodeMasterJpaEntity> countRoot = countQuery.from(CodeMasterJpaEntity.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, command));
        long totalElements = em.createQuery(countQuery).getSingleResult();

        if (totalElements == 0) {
            return PagedResult.of(List.of(), 0L, 0, command.page(), command.size());
        }

        // 데이터 쿼리
        CriteriaQuery<CodeMasterJpaEntity> dataQuery = cb.createQuery(CodeMasterJpaEntity.class);
        Root<CodeMasterJpaEntity> dataRoot = dataQuery.from(CodeMasterJpaEntity.class);
        dataQuery.where(buildPredicates(cb, dataRoot, command));
        // tie-break: masterCode asc, id asc — T1 flaky 방지
        dataQuery.orderBy(cb.asc(dataRoot.get("masterCode")), cb.asc(dataRoot.get("id")));

        TypedQuery<CodeMasterJpaEntity> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult(command.page() * command.size());
        typedQuery.setMaxResults(command.size());

        List<CodeMasterSummary> content = typedQuery.getResultList().stream()
                .map(e -> new CodeMasterSummary(e.getId(), e.getMasterCode(), e.getMasterName(), e.getDescription(), e.getSortOrder(), e.getActive(), e.getUpdatedAt()))
                .toList();

        int totalPages = (int) Math.ceil((double) totalElements / command.size());
        return PagedResult.of(content, totalElements, totalPages, command.page(), command.size());
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        String sql = """
                SELECT master_code, master_name FROM admin.code_master
                WHERE (master_code ILIKE :q || '%' OR master_name ILIKE '%' || :q || '%')
                ORDER BY master_code
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

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CodeMasterJpaEntity> root, SearchCodeMasterCommand command) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(command.masterCode())) {
            predicates.add(cb.like(root.get("masterCode"), command.masterCode() + "%"));
        }
        if (StringUtils.hasText(command.masterName())) {
            predicates.add(cb.like(root.get("masterName"), "%" + command.masterName() + "%"));
        }
        if (command.active() != null) {
            predicates.add(cb.equal(root.get("active"), command.active()));
        }
        return predicates.toArray(Predicate[]::new);
    }
}
