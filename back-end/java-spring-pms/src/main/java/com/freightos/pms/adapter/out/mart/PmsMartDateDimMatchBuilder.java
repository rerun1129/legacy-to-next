package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.adapter.out.mart.document.PmsDocDtEntryDocument;
import com.freightos.pms.adapter.out.mart.document.PmsPerfDtEntryDocument;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * line-grain sidecar용 MongoDB Criteria 빌더.
 * PmsMartDateDimQueryPlanner의 match 구성을 분리한 컴포넌트.
 *
 * freight 컬렉션(pms_perfdt_entry)과 document 컬렉션(pms_docdt_entry)의
 * 각 필드명은 해당 Document 클래스의 실제 필드와 1:1 대응한다.
 *
 * W1-A: FE가 전송하지 않는 필터(hblNo/mblNo/거래처/운송사/항만/영업/비정형)를 제거.
 * 잔존: jobDiv/bound + 날짜 범위 + documentTypes/documentStatus/grouped.
 */
@Component
public class PmsMartDateDimMatchBuilder {

    // ── 공개 빌더 ─────────────────────────────────────────────────────────────

    /**
     * pms_perfdt_entry 컬렉션 match Criteria.
     * flagField ∈ {"hasFreightInput","hasTaxIssued","hasSlipIssued"} — basis에 따라 어댑터가 결정.
     *
     * @see PmsPerfDtEntryDocument
     */
    public Criteria buildFreightMatch(SearchPmsPerformanceCommand c, String flagField) {
        List<Criteria> parts = new ArrayList<>();

        // 1차 가속기: basis flag + 실적일자(pd) 범위 인덱스로 좁힘
        parts.add(Criteria.where(flagField).is(true));
        addPerformanceDateRange(parts, c);

        // 차원 필터(residual): fdcTypes contains
        addFdcTypesFilter(parts, c);

        // B/L 레벨 식별자 필터(residual) — jobDiv/bound만 잔존
        addCommonBlCriteria(parts, c);

        return andAll(parts);
    }

    /**
     * pms_docdt_entry 컬렉션 match Criteria.
     * 날짜는 perfPd(실적일자) 또는 docDt(서류일자) 둘 다 적용 가능.
     *
     * @see PmsDocDtEntryDocument
     */
    public Criteria buildDocumentMatch(SearchPmsPerformanceCommand c) {
        List<Criteria> parts = new ArrayList<>();

        // 1차 가속기: 실적일자(perfPd) + 서류일자(docDt) 범위
        addDocumentDateRanges(parts, c);

        // 서류 차원 필터(residual)
        if (c.documentTypes() != null && !c.documentTypes().isEmpty()) {
            parts.add(Criteria.where("docType").in(c.documentTypes()));
        }
        addEq(parts, "status", c.documentStatus());
        addGroupedFilter(parts, c);

        // B/L 레벨 식별자 필터(residual) — jobDiv/bound만 잔존
        addCommonBlCriteria(parts, c);

        return andAll(parts);
    }

    // ── 날짜 범위 헬퍼 ────────────────────────────────────────────────────────

    /** pd(실적일자) 범위 — PmsPerfDtEntryDocument.pd 필드. */
    private void addPerformanceDateRange(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        boolean hasFrom = StringUtils.hasText(c.performanceDtFrom());
        boolean hasTo = StringUtils.hasText(c.performanceDtTo());
        if (!hasFrom && !hasTo) return;

        Criteria dateCriteria = Criteria.where("pd");
        if (hasFrom && hasTo) {
            parts.add(dateCriteria.gte(c.performanceDtFrom()).lte(c.performanceDtTo()));
        } else if (hasFrom) {
            parts.add(dateCriteria.gte(c.performanceDtFrom()));
        } else {
            parts.add(dateCriteria.lte(c.performanceDtTo()));
        }
    }

    /**
     * perfPd(실적일자)·docDt(서류일자) 각각 독립 적용 — PmsDocDtEntryDocument 필드.
     * 두 범위가 동시에 있으면 둘 다 AND 적용(더 좁은 집합).
     */
    private void addDocumentDateRanges(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        boolean hasPerfFrom = StringUtils.hasText(c.performanceDtFrom());
        boolean hasPerfTo = StringUtils.hasText(c.performanceDtTo());
        if (hasPerfFrom || hasPerfTo) {
            Criteria perfCriteria = Criteria.where("perfPd");
            if (hasPerfFrom && hasPerfTo) {
                parts.add(perfCriteria.gte(c.performanceDtFrom()).lte(c.performanceDtTo()));
            } else if (hasPerfFrom) {
                parts.add(perfCriteria.gte(c.performanceDtFrom()));
            } else {
                parts.add(perfCriteria.lte(c.performanceDtTo()));
            }
        }

        boolean hasDocFrom = StringUtils.hasText(c.documentDtFrom());
        boolean hasDocTo = StringUtils.hasText(c.documentDtTo());
        if (hasDocFrom || hasDocTo) {
            Criteria docCriteria = Criteria.where("docDt");
            if (hasDocFrom && hasDocTo) {
                parts.add(docCriteria.gte(c.documentDtFrom()).lte(c.documentDtTo()));
            } else if (hasDocFrom) {
                parts.add(docCriteria.gte(c.documentDtFrom()));
            } else {
                parts.add(docCriteria.lte(c.documentDtTo()));
            }
        }
    }

    // ── 차원 필터 헬퍼 ─────────────────────────────────────────────────────────

    /**
     * fdcTypes 배열 contains 매칭.
     * documentTypes 다중 선택이면 in().
     * MongoDB 배열 필드에 대한 in은 "배열에 해당 값 포함" 의미로 동작.
     */
    private void addFdcTypesFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (c.documentTypes() != null && !c.documentTypes().isEmpty()) {
            parts.add(Criteria.where("fdcTypes").in(c.documentTypes()));
        }
    }

    /** grouped "Y"/"N" → boolean 변환. */
    private void addGroupedFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.grouped())) return;
        switch (c.grouped()) {
            case "Y" -> parts.add(Criteria.where("grouped").is(true));
            case "N" -> parts.add(Criteria.where("grouped").is(false));
            default -> { /* 미인식값: 필터 무시 */ }
        }
    }

    // ── 공통 B/L 식별자 필터 ─────────────────────────────────────────────────

    /**
     * 두 sidecar 컬렉션에 공통인 B/L 레벨 식별자 필터.
     * FE가 전송하는 jobDiv/bound만 잔존한다.
     */
    private void addCommonBlCriteria(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        addEq(parts, "jobDiv", c.jobDiv());
        addEq(parts, "bound", c.bound());
    }

    // ── 단순 헬퍼 ─────────────────────────────────────────────────────────────

    private void addEq(List<Criteria> parts, String field, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(Criteria.where(field).is(value));
        }
    }

    /** 여러 Criteria를 AND로 합성한다. 단일 항목이면 andOperator 래핑 없이 반환. */
    Criteria andAll(List<Criteria> parts) {
        if (parts.size() == 1) return parts.get(0);
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }
}
