package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.adapter.out.persistence.entity.QPmsFinancialDocumentRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightHeaderRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightLineRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsMasterBlRefJpaEntity;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PMS 조회 WHERE 절 빌더. 두 소스(freight_line, financial_document) 공통 필터를 한 곳에서 관리.
 * null/blank 파라미터는 자동으로 null BooleanExpression 반환 → QueryDSL이 무시.
 */
@Component
public class PmsPerformanceWhereBuilder {

    // ── freight_line 기반 WHERE 절 ─────────────────────────────────────────────

    public BooleanExpression[] buildForFreightLine(
            SearchPmsPerformanceCommand c,
            QPmsFreightLineRefJpaEntity line,
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            // B/L 공통
            eqString(coalesce(houseBl.jobDiv, masterBl.jobDiv), c.jobDiv()),
            eqString(coalesce(houseBl.bound, masterBl.bound), c.bound()),
            dateFilterFrom(houseBl, masterBl, c),
            dateFilterTo(houseBl, masterBl, c),
            dateFrom(line.performanceDt, c.performanceDtFrom()),
            dateTo(line.performanceDt, c.performanceDtTo()),
            likeString(houseBl.hblNo, c.hblNo()),
            likeString(houseBl.mblNo, c.mblNo()),
            // 거래처
            partyFilter(header, houseBl, c),
            eqString(header.actualCustomerCode, c.actualCustomerCode()),
            eqString(header.settlePartnerCode, c.settlePartnerCode()),
            eqString(header.linerCode, c.carrierCode()),
            // 항만
            portFilter(houseBl, masterBl, c),
            // 영업
            eqString(houseBl.salesManCode, c.salesManCode()),
            eqString(houseBl.salesClass, c.salesClass()),
            eqString(houseBl.incoterms, c.incoterms()),
            eqString(houseBl.teamCode, c.teamCode()),
            // BMS 운임행
            documentTypesFilter(line, c.documentTypes()),
            eqString(line.financialDocType, c.financialDocType()),
            eqString(line.taxType, c.taxType()),
            issuedFilter(line, c.issued()),
            // basis 분기 조건은 PmsFreightLineAggregateQueryRepository가 추가
        };
    }

    // ── financial_document 기반 WHERE 절 ─────────────────────────────────────────

    public BooleanExpression[] buildForDocument(
            SearchPmsPerformanceCommand c,
            QPmsFinancialDocumentRefJpaEntity doc,
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            eqString(coalesce(houseBl.jobDiv, masterBl.jobDiv), c.jobDiv()),
            eqString(coalesce(houseBl.bound, masterBl.bound), c.bound()),
            dateFilterFromDoc(houseBl, masterBl, c),
            dateFilterToDoc(houseBl, masterBl, c),
            dateFrom(doc.performanceDt, c.performanceDtFrom()),
            dateTo(doc.performanceDt, c.performanceDtTo()),
            likeString(houseBl.hblNo, c.hblNo()),
            likeString(houseBl.mblNo, c.mblNo()),
            eqString(header.actualCustomerCode, c.actualCustomerCode()),
            eqString(header.settlePartnerCode, c.settlePartnerCode()),
            eqString(header.linerCode, c.carrierCode()),
            portFilter(houseBl, masterBl, c),
            eqString(doc.teamCode, c.teamCode()),
            eqString(doc.operator, c.operator()),
            documentTypesFilter(doc, c.documentTypes()),
            eqString(doc.documentStatus, c.documentStatus()),
            likeString(doc.documentNo, c.documentNoLike()),
            dateFrom(doc.documentDt, c.documentDtFrom()),
            dateTo(doc.documentDt, c.documentDtTo()),
            likeString(doc.groupFinancialNo, c.groupFinancialNo()),
            groupedFilter(doc.groupFinancialNo, c.grouped()),
        };
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────────────────────

    public BooleanExpression eqString(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.eq(value);
    }

    public BooleanExpression eqString(StringExpression expr, String value) {
        return (value == null || value.isBlank()) ? null : expr.eq(value);
    }

    public BooleanExpression likeString(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.containsIgnoreCase(value);
    }

    public BooleanExpression dateFrom(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.goe(value);
    }

    public BooleanExpression dateTo(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.loe(value);
    }

    public StringExpression coalesce(StringPath houseVal, StringPath masterVal) {
        return Expressions.stringTemplate("coalesce({0}, {1})", houseVal, masterVal);
    }

    // ── 복합 필터 헬퍼 ─────────────────────────────────────────────────────────────

    /** dateKind 기반 ETD/ETA/PERFORMANCE 일자 from 분기. freight_line 버전. */
    private BooleanExpression dateFilterFrom(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        String from = c.dateFrom();
        if (kind == null || from == null || from.isBlank()) return null;
        return switch (kind) {
            case "ETD" -> coalesce(houseBl.etd, masterBl.etd).goe(from);
            case "ETA" -> coalesce(houseBl.eta, masterBl.eta).goe(from);
            default -> null;
        };
    }

    private BooleanExpression dateFilterTo(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        String to = c.dateTo();
        if (kind == null || to == null || to.isBlank()) return null;
        return switch (kind) {
            case "ETD" -> coalesce(houseBl.etd, masterBl.etd).loe(to);
            case "ETA" -> coalesce(houseBl.eta, masterBl.eta).loe(to);
            default -> null;
        };
    }

    /** dateKind 기반 ETD/ETA 일자 from 분기. document 버전(performance는 별도 필드). */
    private BooleanExpression dateFilterFromDoc(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        return dateFilterFrom(houseBl, masterBl, c);
    }

    private BooleanExpression dateFilterToDoc(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        return dateFilterTo(houseBl, masterBl, c);
    }

    /** partyKind 기반 거래처 코드 분기. */
    private BooleanExpression partyFilter(
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.partyKind();
        String code = c.partyCode();
        if (kind == null || code == null || code.isBlank()) return null;
        return switch (kind) {
            case "ACTUAL_CUSTOMER" -> header.actualCustomerCode.eq(code);
            case "SETTLE_PARTNER" -> header.settlePartnerCode.eq(code);
            default -> null;
        };
    }

    /** portKind 기반 항만 코드 분기. */
    private BooleanExpression portFilter(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.portKind();
        String code = c.portCode();
        if (kind == null || code == null || code.isBlank()) return null;
        return switch (kind) {
            case "POL" -> coalesce(houseBl.polCode, masterBl.polCode).eq(code);
            case "POD" -> coalesce(houseBl.podCode, masterBl.podCode).eq(code);
            default -> null;
        };
    }

    /** freight_line.financial_document_id IS (NOT) NULL 발급 여부 필터. */
    private BooleanExpression issuedFilter(QPmsFreightLineRefJpaEntity line, String issued) {
        if (issued == null || issued.isBlank()) return null;
        return switch (issued.toUpperCase()) {
            case "Y" -> line.financialDocumentId.isNotNull();
            case "N" -> line.financialDocumentId.isNull();
            default -> null;
        };
    }

    /** document_type IN 필터. freight_line 버전. */
    private BooleanExpression documentTypesFilter(
            QPmsFreightLineRefJpaEntity line, List<String> types) {
        if (types == null || types.isEmpty()) return null;
        return line.financialDocType.in(types);
    }

    /** document_type IN 필터. financial_document 버전. */
    private BooleanExpression documentTypesFilter(
            QPmsFinancialDocumentRefJpaEntity doc, List<String> types) {
        if (types == null || types.isEmpty()) return null;
        return doc.documentType.in(types);
    }

    /** grouped(Y/N) 필터. group_financial_no IS (NOT) NULL 변환. */
    private BooleanExpression groupedFilter(StringPath path, String grouped) {
        if (grouped == null || grouped.isBlank()) return null;
        return switch (grouped.toUpperCase()) {
            case "Y" -> path.isNotNull();
            case "N" -> path.isNull();
            default -> null;
        };
    }
}
