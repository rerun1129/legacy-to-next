package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.adapter.out.persistence.entity.QPmsFinancialDocumentRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightHeaderRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightLineRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsMasterBlRefJpaEntity;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PMS 조회 WHERE 절 빌더.
 * <p>
 * freight_line/header 전용 술어(freightLineHeaderPredicates)와
 * FMS B/L 테이블 술어(fmsPredicates)를 분리 제공한다.
 * FMS 조인이 필요 없을 때는 fmsPredicates를 적용하지 않으므로
 * 불필요한 FMS JOIN + coalesce 연산을 완전히 제거할 수 있다.
 * <p>
 * ETD/ETA 조건은 인덱스를 활용할 수 있도록 sargable 형태로 작성한다.
 * (coalesce(house.eta, master.eta) 비교 금지 → 테이블별 OR 조건)
 */
@Component
public class PmsPerformanceWhereBuilder {

    // ── freight_line / header 전용 술어 (FMS 조인 불필요) ─────────────────────────

    /**
     * freight_line 기반 쿼리에서 항상 적용되는 술어.
     * freight_line 자체 필터 + freight_header 필터 + performanceDt 범위 포함.
     * FMS(house_bl/master_bl) 컬럼은 포함하지 않는다.
     */
    public BooleanExpression[] freightLineHeaderPredicates(
            SearchPmsPerformanceCommand c,
            QPmsFreightLineRefJpaEntity line,
            QPmsFreightHeaderRefJpaEntity header) {

        return new BooleanExpression[] {
            eqString(header.actualCustomerCode, c.actualCustomerCode()),
            eqString(header.settlePartnerCode, c.settlePartnerCode()),
            eqString(header.linerCode, c.carrierCode()),
            dateFrom(line.performanceDt, c.performanceDtFrom()),
            dateTo(line.performanceDt, c.performanceDtTo()),
            documentTypesFilter(line, c.documentTypes()),
            eqString(line.financialDocType, c.financialDocType()),
            eqString(line.taxType, c.taxType()),
            issuedFilter(line, c.issued()),
        };
    }

    /**
     * financial_document 기반 쿼리에서 항상 적용되는 술어.
     * document 자체 필터 + freight_header 필터 포함.
     * FMS(house_bl/master_bl) 컬럼은 포함하지 않는다.
     */
    public BooleanExpression[] documentHeaderPredicates(
            SearchPmsPerformanceCommand c,
            QPmsFinancialDocumentRefJpaEntity doc,
            QPmsFreightHeaderRefJpaEntity header) {

        return new BooleanExpression[] {
            eqString(header.actualCustomerCode, c.actualCustomerCode()),
            eqString(header.settlePartnerCode, c.settlePartnerCode()),
            eqString(header.linerCode, c.carrierCode()),
            dateFrom(doc.performanceDt, c.performanceDtFrom()),
            dateTo(doc.performanceDt, c.performanceDtTo()),
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

    // ── FMS B/L 술어 (FMS 조인이 있을 때만 적용) ────────────────────────────────

    /**
     * freight_line 기반 쿼리에서 FMS 조인이 있을 때만 추가되는 술어.
     * 교차 테이블 동등 비교(jobDiv/bound 등)는 (house.col = ? OR master.col = ?) OR 형태.
     * LEFT JOIN 행에서 반대 테이블 컬럼은 NULL이므로 AND 결합 시 전 행 제외됨을 방지.
     * ETD/ETA는 sargable OR 형태로 작성해 인덱스를 활용한다.
     */
    public BooleanExpression[] fmsPredicatesForFreightLine(
            SearchPmsPerformanceCommand c,
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            crossTableEq(houseBl.jobDiv, masterBl.jobDiv, c.jobDiv()),
            crossTableEq(houseBl.bound, masterBl.bound, c.bound()),
            sargableDateFrom(houseBl, masterBl, c),
            sargableDateTo(houseBl, masterBl, c),
            likeString(houseBl.hblNo, c.hblNo()),
            likeString(houseBl.mblNo, c.mblNo()),
            partyFilter(header, c),
            portFilter(houseBl, masterBl, c),
            eqString(houseBl.salesManCode, c.salesManCode()),
            eqString(houseBl.salesClass, c.salesClass()),
            eqString(houseBl.incoterms, c.incoterms()),
            eqString(houseBl.teamCode, c.teamCode()),
        };
    }

    /**
     * financial_document 기반 쿼리에서 FMS 조인이 있을 때만 추가되는 술어.
     */
    public BooleanExpression[] fmsPredicatesForDocument(
            SearchPmsPerformanceCommand c,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            crossTableEq(houseBl.jobDiv, masterBl.jobDiv, c.jobDiv()),
            crossTableEq(houseBl.bound, masterBl.bound, c.bound()),
            sargableDateFrom(houseBl, masterBl, c),
            sargableDateTo(houseBl, masterBl, c),
            likeString(houseBl.hblNo, c.hblNo()),
            likeString(houseBl.mblNo, c.mblNo()),
            portFilter(houseBl, masterBl, c),
        };
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────────────────────

    public BooleanExpression eqString(StringPath path, String value) {
        return hasValue(value) ? path.eq(value) : null;
    }

    public BooleanExpression likeString(StringPath path, String value) {
        return hasValue(value) ? path.containsIgnoreCase(value) : null;
    }

    public BooleanExpression dateFrom(StringPath path, String value) {
        return hasValue(value) ? path.goe(value) : null;
    }

    public BooleanExpression dateTo(StringPath path, String value) {
        return hasValue(value) ? path.loe(value) : null;
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }

    // ── 복합 필터 헬퍼 ─────────────────────────────────────────────────────────────

    /**
     * 두 테이블에 걸쳐 동일 값을 비교하는 OR 조건.
     * LEFT JOIN에서 한쪽 테이블 컬럼은 NULL이므로 AND가 아닌 OR로 결합해야 한다.
     * (house.col = ? OR master.col = ?)
     */
    private BooleanExpression crossTableEq(StringPath houseCol, StringPath masterCol, String value) {
        if (!hasValue(value)) return null;
        return houseCol.eq(value).or(masterCol.eq(value));
    }

    /**
     * sargable ETD/ETA from 조건.
     * coalesce 대신 (house.field >= from OR master.field >= from) 형태로
     * 각 테이블 인덱스를 활용 가능하게 한다.
     */
    private BooleanExpression sargableDateFrom(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        String from = c.dateFrom();
        if (!hasValue(kind) || !hasValue(from)) return null;
        return switch (kind) {
            case "ETD" -> houseBl.etd.goe(from).or(masterBl.etd.goe(from));
            case "ETA" -> houseBl.eta.goe(from).or(masterBl.eta.goe(from));
            default -> null;
        };
    }

    /**
     * sargable ETD/ETA to 조건.
     */
    private BooleanExpression sargableDateTo(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.dateKind();
        String to = c.dateTo();
        if (!hasValue(kind) || !hasValue(to)) return null;
        return switch (kind) {
            case "ETD" -> houseBl.etd.loe(to).or(masterBl.etd.loe(to));
            case "ETA" -> houseBl.eta.loe(to).or(masterBl.eta.loe(to));
            default -> null;
        };
    }

    /** partyKind 기반 거래처 코드 분기. */
    private BooleanExpression partyFilter(QPmsFreightHeaderRefJpaEntity header, SearchPmsPerformanceCommand c) {
        String kind = c.partyKind();
        String code = c.partyCode();
        if (!hasValue(kind) || !hasValue(code)) return null;
        return switch (kind) {
            case "ACTUAL_CUSTOMER" -> header.actualCustomerCode.eq(code);
            case "SETTLE_PARTNER" -> header.settlePartnerCode.eq(code);
            default -> null;
        };
    }

    /** portKind 기반 항만 코드 분기. sargable: 각 테이블 컬럼 직접 비교. */
    private BooleanExpression portFilter(
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            SearchPmsPerformanceCommand c) {
        String kind = c.portKind();
        String code = c.portCode();
        if (!hasValue(kind) || !hasValue(code)) return null;
        return switch (kind) {
            case "POL" -> houseBl.polCode.eq(code).or(masterBl.polCode.eq(code));
            case "POD" -> houseBl.podCode.eq(code).or(masterBl.podCode.eq(code));
            default -> null;
        };
    }

    /** freight_line.financial_document_id IS (NOT) NULL 발급 여부 필터. */
    private BooleanExpression issuedFilter(QPmsFreightLineRefJpaEntity line, String issued) {
        if (!hasValue(issued)) return null;
        return switch (issued.toUpperCase()) {
            case "Y" -> line.financialDocumentId.isNotNull();
            case "N" -> line.financialDocumentId.isNull();
            default -> null;
        };
    }

    /** document_type IN 필터. freight_line 버전. */
    private BooleanExpression documentTypesFilter(QPmsFreightLineRefJpaEntity line, List<String> types) {
        if (types == null || types.isEmpty()) return null;
        return line.financialDocType.in(types);
    }

    /** document_type IN 필터. financial_document 버전. */
    private BooleanExpression documentTypesFilter(QPmsFinancialDocumentRefJpaEntity doc, List<String> types) {
        if (types == null || types.isEmpty()) return null;
        return doc.documentType.in(types);
    }

    /** grouped(Y/N) 필터. group_financial_no IS (NOT) NULL 변환. */
    private BooleanExpression groupedFilter(StringPath path, String grouped) {
        if (!hasValue(grouped)) return null;
        return switch (grouped.toUpperCase()) {
            case "Y" -> path.isNotNull();
            case "N" -> path.isNull();
            default -> null;
        };
    }
}
