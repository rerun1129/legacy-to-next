package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * SearchPmsPerformanceCommand → MongoDB Criteria 변환기.
 * OLTP WhereBuilder의 필터 의미와 1:1 대응하되 MongoDB 경로로 매핑.
 *
 * W1-A: FE가 전송하지 않는 20개 필터를 제거. jobDiv/bound 차원만 유지.
 */
@Component
public class PmsMartCriteriaBuilder {

    /**
     * freight_line 기반 Mart 검색 Criteria.
     * existFlagField(hasFreightInput/hasTaxIssued/hasSlipIssued)로 해당 basis 존재 행만 조회.
     */
    public Criteria buildFreight(SearchPmsPerformanceCommand c, String existFlagField) {
        List<Criteria> parts = new ArrayList<>();
        parts.add(Criteria.where(existFlagField).is(true));
        addCommonIdentifierCriteria(c, parts);
        return andAll(parts);
    }

    /**
     * financial_document 기반 Mart 검색 Criteria.
     * documentCreated 블록 존재 여부 + 공통 식별자 필터.
     */
    public Criteria buildDocument(SearchPmsPerformanceCommand c) {
        List<Criteria> parts = new ArrayList<>();
        parts.add(Criteria.where("hasDocumentCreated").is(true));
        addCommonIdentifierCriteria(c, parts);
        return andAll(parts);
    }

    // ── 공통 식별자 필터 ──────────────────────────────────────────────────────

    /**
     * 두 basis 경로(freight/document)에 공통으로 적용되는 B/L 식별자 레벨 필터.
     * FE가 실제로 전송하는 jobDiv/bound + ETD/ETA 날짜 범위만 포함한다.
     */
    private void addCommonIdentifierCriteria(SearchPmsPerformanceCommand c, List<Criteria> parts) {
        addEq(parts, "jobDiv", c.jobDiv());
        addEq(parts, "bound", c.bound());
        addDateRange(parts, c);
    }

    // ── 날짜 범위 헬퍼 ─────────────────────────────────────────────────────────

    /**
     * ETD/ETA 범위 필터.
     * yyyyMMdd 문자열 사전순 비교가 날짜 비교와 동일하게 동작함을 이용.
     */
    private void addDateRange(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        if (!StringUtils.hasText(kind)) return;

        String field = switch (kind) {
            case "ETD" -> "etd";
            case "ETA" -> "eta";
            default -> null;
        };
        if (field == null) return;

        Criteria dateCriteria = Criteria.where(field);
        boolean hasFrom = StringUtils.hasText(c.dateFrom());
        boolean hasTo = StringUtils.hasText(c.dateTo());
        if (hasFrom && hasTo) {
            parts.add(dateCriteria.gte(c.dateFrom()).lte(c.dateTo()));
        } else if (hasFrom) {
            parts.add(dateCriteria.gte(c.dateFrom()));
        } else if (hasTo) {
            parts.add(dateCriteria.lte(c.dateTo()));
        }
    }

    // ── 단순 헬퍼 ─────────────────────────────────────────────────────────────

    private void addEq(List<Criteria> parts, String field, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(Criteria.where(field).is(value));
        }
    }

    /** 여러 Criteria를 AND로 합성한다. 단일 항목이면 andOperator 래핑 없이 반환. */
    private Criteria andAll(List<Criteria> parts) {
        if (parts.size() == 1) return parts.get(0);
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }
}
