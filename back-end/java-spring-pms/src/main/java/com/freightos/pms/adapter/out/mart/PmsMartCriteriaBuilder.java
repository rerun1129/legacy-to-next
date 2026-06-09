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

        // teamCode는 HOUSE 전용 필드 houseTeamCode로 매핑
        if (StringUtils.hasText(c.teamCode())) {
            parts.add(Criteria.where("houseTeamCode").is(c.teamCode()));
        }

        return andAll(parts);
    }

    /**
     * financial_document 기반 Mart 검색 Criteria.
     * documentCreated 블록 존재 여부 + 공통 식별자 필터 + teamCode/operator는 documentCreated 서브문서 경로.
     */
    public Criteria buildDocument(SearchPmsPerformanceCommand c) {
        List<Criteria> parts = new ArrayList<>();
        parts.add(Criteria.where("hasDocumentCreated").is(true));
        addCommonIdentifierCriteria(c, parts);

        // document 경로: teamCode/operator는 documentCreated 서브문서 필드
        if (StringUtils.hasText(c.teamCode())) {
            parts.add(Criteria.where("documentCreated.teamCode").is(c.teamCode()));
        }
        if (StringUtils.hasText(c.operator())) {
            parts.add(Criteria.where("documentCreated.operator").is(c.operator()));
        }

        return andAll(parts);
    }

    // ── 공통 식별자 필터 ──────────────────────────────────────────────────────

    /**
     * 두 basis 경로(freight/document)에 공통으로 적용되는 B/L 식별자 레벨 필터.
     * OLTP WhereBuilder에서 h.*(freight_header) · hb./mb.(B/L) 컬럼에 거는 술어와 1:1 대응.
     * null/blank 값은 건너뛴다.
     */
    private void addCommonIdentifierCriteria(SearchPmsPerformanceCommand c, List<Criteria> parts) {
        // jobDiv, bound — 공통 필드 직접 대응
        addEq(parts, "jobDiv", c.jobDiv());
        addEq(parts, "bound", c.bound());

        // dateKind + dateFrom/dateTo — ETD/ETA 범위 (PERFORMANCE는 line-level이므로 Mart 미지원)
        addDateRange(parts, c);

        // B/L 번호 — prefix case-sensitive (입력은 대문자 정규화 전제)
        addRegex(parts, "houseBlNo", c.hblNo());
        addRegex(parts, "masterBlNo", c.mblNo());

        // 거래처 직접 코드
        addEq(parts, "actualCustomerCode", c.actualCustomerCode());
        addEq(parts, "settlePartnerCode", c.settlePartnerCode());

        // partyKind + partyCode 동적 분기 — OLTP partyFilter 로직과 동일
        addPartyFilter(parts, c);

        // 운송사
        addEq(parts, "linerCode", c.carrierCode());

        // portKind + portCode 동적 분기
        addPortFilter(parts, c);

        // 영업 필드 (HOUSE 전용 — MASTER 행에는 null 저장되어 있으므로 자연 필터)
        addEq(parts, "salesManCode", c.salesManCode());
        addEq(parts, "incoterms", c.incoterms());
        addEq(parts, "salesClass", c.salesClass());
    }

    // ── 날짜 범위 헬퍼 ─────────────────────────────────────────────────────────

    /**
     * ETD/ETA 범위 필터. PERFORMANCE dateKind는 line-level 필터이므로 Mart에서 무시한다.
     * yyyyMMdd 문자열 사전순 비교가 날짜 비교와 동일하게 동작함을 이용.
     */
    private void addDateRange(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        if (!StringUtils.hasText(kind)) return;

        String field = switch (kind) {
            case "ETD" -> "etd";
            case "ETA" -> "eta";
            default -> null;  // PERFORMANCE 등 line-level 필터는 Mart 미지원이므로 무시
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

    // ── 동적 필터 헬퍼 ────────────────────────────────────────────────────────

    /** partyKind 기반 거래처 코드 분기. OLTP partyFilter 로직과 동일. */
    private void addPartyFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.partyKind()) || !StringUtils.hasText(c.partyCode())) return;
        switch (c.partyKind()) {
            case "ACTUAL_CUSTOMER" -> parts.add(Criteria.where("actualCustomerCode").is(c.partyCode()));
            case "SETTLE_PARTNER"  -> parts.add(Criteria.where("settlePartnerCode").is(c.partyCode()));
            default -> { /* 미인식 partyKind: 필터 무시 */ }
        }
    }

    /** portKind 기반 항만 코드 분기. OLTP portFilter 로직과 동일. */
    private void addPortFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.portKind()) || !StringUtils.hasText(c.portCode())) return;
        switch (c.portKind()) {
            case "POL" -> parts.add(Criteria.where("polCode").is(c.portCode()));
            case "POD" -> parts.add(Criteria.where("podCode").is(c.portCode()));
            default -> { /* 미인식 portKind: 필터 무시 */ }
        }
    }

    // ── 단순 헬퍼 ─────────────────────────────────────────────────────────────

    private void addEq(List<Criteria> parts, String field, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(Criteria.where(field).is(value));
        }
    }

    /** prefix case-sensitive 정규식 필터. PmsBlNoMatch 공용 헬퍼 위임. */
    private void addRegex(List<Criteria> parts, String field, String value) {
        if (StringUtils.hasText(value)) {
            parts.add(PmsBlNoMatch.prefixCriteria(field, value));
        }
    }

    /** 여러 Criteria를 AND로 합성한다. 단일 항목이면 andOperator 래핑 없이 반환. */
    private Criteria andAll(List<Criteria> parts) {
        if (parts.size() == 1) return parts.get(0);
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }
}
