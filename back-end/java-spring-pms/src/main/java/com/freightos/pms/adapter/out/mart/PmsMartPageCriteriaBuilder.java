package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 적응형 page 경로(조기종료 find)용 pms_bl_mart Criteria 빌더.
 *
 * 기존 fast-path의 PmsMartCriteriaBuilder(B/L 레벨 + basis flag)에
 * lines/docs $elemMatch 원소 조건을 AND로 합성하여 반환한다.
 *
 * 사용 조건: count > earlyTermThreshold (밀집 범위) 인 경우만 호출된다.
 */
@Component
public class PmsMartPageCriteriaBuilder {

    private final PmsMartCriteriaBuilder base;

    public PmsMartPageCriteriaBuilder(PmsMartCriteriaBuilder base) {
        this.base = base;
    }

    // ── freight page criteria ─────────────────────────────────────────────────

    /**
     * freight basis 밀집 경로용 pms_bl_mart Criteria.
     * base(flag + B/L 레벨 식별자) AND lines $elemMatch(실적일자 범위 + basis flag + docType).
     *
     * @param flagField "hasFreightInput" / "hasTaxIssued" / "hasSlipIssued"
     * @param basisKey  "freightInput" / "taxIssued" / "slipIssued"
     */
    public Criteria buildFreightPageCriteria(
            SearchPmsPerformanceCommand c,
            String basisKey,
            String flagField) {

        Criteria blLevel = base.buildFreight(c, flagField);
        Criteria lineElem = buildFreightElemMatch(c, basisKey);
        return new Criteria().andOperator(blLevel, lineElem);
    }

    /**
     * lines $elemMatch 원소 조건 빌드.
     * performanceDtFrom/To → pd gte/lte (단일 Criteria 체이닝으로 동일 키 중복 회피).
     * basis flag → tax/slip boolean.
     * documentTypes/financialDocType → fdcType in/is.
     */
    private Criteria buildFreightElemMatch(SearchPmsPerformanceCommand c, String basisKey) {
        List<Criteria> elemParts = new ArrayList<>();

        // 실적일자 범위 — pd 단일 Criteria에 gte/lte 체이닝(동일 키 andOperator 중복 방지)
        boolean hasPerfFrom = StringUtils.hasText(c.performanceDtFrom());
        boolean hasPerfTo   = StringUtils.hasText(c.performanceDtTo());
        if (hasPerfFrom && hasPerfTo) {
            elemParts.add(Criteria.where("pd").gte(c.performanceDtFrom()).lte(c.performanceDtTo()));
        } else if (hasPerfFrom) {
            elemParts.add(Criteria.where("pd").gte(c.performanceDtFrom()));
        } else if (hasPerfTo) {
            elemParts.add(Criteria.where("pd").lte(c.performanceDtTo()));
        }

        // basis flag — freightInput은 조건 없음(전체), taxIssued/slipIssued는 boolean 필드 필터
        switch (basisKey) {
            case "taxIssued"  -> elemParts.add(Criteria.where("tax").is(true));
            case "slipIssued" -> elemParts.add(Criteria.where("slip").is(true));
            default -> { /* freightInput: 필터 없음 */ }
        }

        // docType 필터
        List<String> types = c.documentTypes();
        if (types != null && !types.isEmpty()) {
            elemParts.add(Criteria.where("fdcType").in(types));
        } else if (StringUtils.hasText(c.financialDocType())) {
            elemParts.add(Criteria.where("fdcType").is(c.financialDocType()));
        }

        return buildElemMatch("lines", elemParts);
    }

    // ── document page criteria ────────────────────────────────────────────────

    /**
     * document basis 밀집 경로용 pms_bl_mart Criteria.
     * B/L 레벨 식별자(teamCode/operator 제외)를 직접 구성하고
     * docs $elemMatch(날짜 범위 + docType/status/grouped/team/operator)를 AND 합성한다.
     *
     * PmsMartCriteriaBuilder.buildDocument는 teamCode를 documentCreated.teamCode(사전집계 경로)로 매핑하므로
     * 재사용 불가. B/L 식별자만 직접 구성한다.
     */
    public Criteria buildDocumentPageCriteria(SearchPmsPerformanceCommand c) {
        Criteria blLevel = buildDocumentBlLevelCriteria(c);
        Criteria docElem = buildDocumentElemMatch(c);
        return new Criteria().andOperator(blLevel, docElem);
    }

    /**
     * document 경로 B/L 레벨 식별자 Criteria.
     * hasDocumentCreated flag + 공통 B/L 식별자(teamCode/operator는 제외 — docs $elemMatch에서 처리).
     */
    private Criteria buildDocumentBlLevelCriteria(SearchPmsPerformanceCommand c) {
        List<Criteria> parts = new ArrayList<>();
        parts.add(Criteria.where("hasDocumentCreated").is(true));

        addEq(parts, "jobDiv", c.jobDiv());
        addEq(parts, "bound", c.bound());
        addDateRange(parts, c);
        addRegex(parts, "houseBlNo", c.hblNo());
        addRegex(parts, "masterBlNo", c.mblNo());
        addEq(parts, "actualCustomerCode", c.actualCustomerCode());
        addEq(parts, "settlePartnerCode", c.settlePartnerCode());
        addPartyFilter(parts, c);
        addEq(parts, "linerCode", c.carrierCode());
        addPortFilter(parts, c);
        addEq(parts, "salesManCode", c.salesManCode());
        addEq(parts, "incoterms", c.incoterms());
        addEq(parts, "salesClass", c.salesClass());

        return andAll(parts);
    }

    /**
     * docs $elemMatch 원소 조건 빌드.
     * 실적일자(perfPd)/서류일자(docDt) 각각 단일 Criteria 체이닝.
     * docType/status/grouped/team/operator 포함.
     */
    private Criteria buildDocumentElemMatch(SearchPmsPerformanceCommand c) {
        List<Criteria> elemParts = new ArrayList<>();

        // 실적일자 범위 — perfPd 단일 Criteria
        boolean hasPerfFrom = StringUtils.hasText(c.performanceDtFrom());
        boolean hasPerfTo   = StringUtils.hasText(c.performanceDtTo());
        if (hasPerfFrom && hasPerfTo) {
            elemParts.add(Criteria.where("perfPd").gte(c.performanceDtFrom()).lte(c.performanceDtTo()));
        } else if (hasPerfFrom) {
            elemParts.add(Criteria.where("perfPd").gte(c.performanceDtFrom()));
        } else if (hasPerfTo) {
            elemParts.add(Criteria.where("perfPd").lte(c.performanceDtTo()));
        }

        // 서류일자 범위 — docDt 단일 Criteria
        boolean hasDocFrom = StringUtils.hasText(c.documentDtFrom());
        boolean hasDocTo   = StringUtils.hasText(c.documentDtTo());
        if (hasDocFrom && hasDocTo) {
            elemParts.add(Criteria.where("docDt").gte(c.documentDtFrom()).lte(c.documentDtTo()));
        } else if (hasDocFrom) {
            elemParts.add(Criteria.where("docDt").gte(c.documentDtFrom()));
        } else if (hasDocTo) {
            elemParts.add(Criteria.where("docDt").lte(c.documentDtTo()));
        }

        // docType
        List<String> types = c.documentTypes();
        if (types != null && !types.isEmpty()) {
            elemParts.add(Criteria.where("docType").in(types));
        }

        // status
        if (StringUtils.hasText(c.documentStatus())) {
            elemParts.add(Criteria.where("status").is(c.documentStatus()));
        }

        // grouped Y/N → boolean
        if (StringUtils.hasText(c.grouped())) {
            switch (c.grouped()) {
                case "Y" -> elemParts.add(Criteria.where("grouped").is(true));
                case "N" -> elemParts.add(Criteria.where("grouped").is(false));
                default  -> { /* 미인식값: 필터 무시 */ }
            }
        }

        // team/operator — docs 원소 레벨 필터
        if (StringUtils.hasText(c.teamCode())) {
            elemParts.add(Criteria.where("team").is(c.teamCode()));
        }
        if (StringUtils.hasText(c.operator())) {
            elemParts.add(Criteria.where("operator").is(c.operator()));
        }

        return buildElemMatch("docs", elemParts);
    }

    // ── 공통 헬퍼 ────────────────────────────────────────────────────────────

    /**
     * elemParts로 $elemMatch Criteria를 구성한다.
     * 원소 조건이 없으면 필드 존재 여부만(size > 0) 검사한다.
     */
    private Criteria buildElemMatch(String arrayField, List<Criteria> elemParts) {
        if (elemParts.isEmpty()) {
            // 날짜/차원 조건 없이 밀집 경로 진입은 사실상 발생하지 않지만 방어적 처리
            return Criteria.where(arrayField).exists(true);
        }
        Criteria elemCondition = elemParts.size() == 1
            ? elemParts.get(0)
            : new Criteria().andOperator(elemParts.toArray(new Criteria[0]));
        return Criteria.where(arrayField).elemMatch(elemCondition);
    }

    /** ETD/ETA 범위 필터 — PmsMartCriteriaBuilder.addDateRange 와 동일 의미. */
    private void addDateRange(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        if (!StringUtils.hasText(kind)) return;
        String field = switch (kind) {
            case "ETD" -> "etd";
            case "ETA" -> "eta";
            default -> null;
        };
        if (field == null) return;

        boolean hasFrom = StringUtils.hasText(c.dateFrom());
        boolean hasTo   = StringUtils.hasText(c.dateTo());
        if (!hasFrom && !hasTo) return;

        Criteria dateCriteria = Criteria.where(field);
        if (hasFrom && hasTo) {
            parts.add(dateCriteria.gte(c.dateFrom()).lte(c.dateTo()));
        } else if (hasFrom) {
            parts.add(dateCriteria.gte(c.dateFrom()));
        } else {
            parts.add(dateCriteria.lte(c.dateTo()));
        }
    }

    private void addPartyFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.partyKind()) || !StringUtils.hasText(c.partyCode())) return;
        switch (c.partyKind()) {
            case "ACTUAL_CUSTOMER" -> parts.add(Criteria.where("actualCustomerCode").is(c.partyCode()));
            case "SETTLE_PARTNER"  -> parts.add(Criteria.where("settlePartnerCode").is(c.partyCode()));
            default -> { /* 미인식 partyKind: 필터 무시 */ }
        }
    }

    private void addPortFilter(List<Criteria> parts, SearchPmsPerformanceCommand c) {
        if (!StringUtils.hasText(c.portKind()) || !StringUtils.hasText(c.portCode())) return;
        switch (c.portKind()) {
            case "POL" -> parts.add(Criteria.where("polCode").is(c.portCode()));
            case "POD" -> parts.add(Criteria.where("podCode").is(c.portCode()));
            default -> { /* 미인식 portKind: 필터 무시 */ }
        }
    }

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

    private Criteria andAll(List<Criteria> parts) {
        if (parts.size() == 1) return parts.get(0);
        return new Criteria().andOperator(parts.toArray(new Criteria[0]));
    }
}
