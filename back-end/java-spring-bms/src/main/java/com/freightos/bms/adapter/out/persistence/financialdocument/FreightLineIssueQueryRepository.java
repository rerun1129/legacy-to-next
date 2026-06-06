package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.adapter.out.persistence.codename.entity.QHouseBlRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QMasterBlRefJpaEntity;
import com.freightos.bms.application.financialdocument.FreightLineIssueRowView;
import com.freightos.bms.application.financialdocument.SearchFreightLineCriteria;
import com.freightos.bms.application.financialdocument.port.out.DocumentLineFlag;
import com.freightos.bms.application.financialdocument.port.out.FreightLineIssueSnapshot;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 운임 행 발급 화면 전용 QueryDSL 레포지토리.
 * ① 전역 조회(발급 화면 그리드) ② 발급용 스냅샷 로드 ③ 발급 상태 집계.
 * FinancialDocumentSearchQueryRepository 패턴 준용 — cross-schema leftJoin.
 * S5: line-level 1:1, selectDistinct·countDistinct 사용 금지, count = freightLineId.count().
 * S5: financial_document inner join(결정1: doc_id IS NOT NULL 고정).
 */
@Repository
@RequiredArgsConstructor
public class FreightLineIssueQueryRepository {

    private final JPAQueryFactory queryFactory;

    // ── 전역 조회 ─────────────────────────────────────────────────────────────

    /**
     * 발급 화면 운임 행 전역 조회. financial_document_id IS NOT NULL 고정(결정1).
     * line-level 1:1 → count = freightLineId.count().
     */
    public Page<FreightLineIssueRowView> searchFreightLines(
            SearchFreightLineCriteria criteria, Pageable pageable) {

        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        QFreightHeaderRefJpaEntity header = QFreightHeaderRefJpaEntity.freightHeaderRefJpaEntity;
        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;
        QHouseBlRefJpaEntity houseBl = QHouseBlRefJpaEntity.houseBlRefJpaEntity;
        QMasterBlRefJpaEntity masterBl = QMasterBlRefJpaEntity.masterBlRefJpaEntity;

        BooleanExpression[] where = buildWhere(criteria, line, houseBl, masterBl);

        // count 쿼리 — line-level 1:1(S5)
        Long countResult = queryFactory
            .select(line.freightLineId.count())
            .from(line)
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .join(doc).on(doc.financialDocumentId.eq(line.financialDocumentId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .fetchOne();
        long total = countResult != null ? countResult : 0L;

        // content 쿼리
        List<Tuple> rows = queryFactory
            .select(
                line.freightLineId, line.freightHeaderId,
                header.blType, header.blId,
                houseBl.hblNo, masterBl.mblNo,
                houseBl.jobDiv, masterBl.jobDiv,
                houseBl.bound, masterBl.bound,
                houseBl.etd, masterBl.etd,
                line.freightType, line.financialDocType, line.freightCode,
                line.customerCode, line.currency,
                line.settleAmount, line.localAmount, line.settleTaxAmount,
                line.localTaxAmount, line.usdAmount,
                line.performanceDt, line.financialDocumentId,
                doc.documentNo, doc.documentStatus,
                line.taxNo, line.taxDt, line.slipNo, line.slipDt
            )
            .from(line)
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .join(doc).on(doc.financialDocumentId.eq(line.financialDocumentId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .orderBy(line.freightLineId.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<FreightLineIssueRowView> content = rows.stream()
            .map(t -> toRowView(t, line, header, doc, houseBl, masterBl))
            .toList();

        return new PageImpl<>(content, pageable, total);
    }

    // ── 발급용 스냅샷 로드 ────────────────────────────────────────────────────

    /**
     * 발급 검증용 스냅샷 로드. Tuple projection — 1차캐시 staleness 회피(S6).
     */
    public List<FreightLineIssueSnapshot> loadIssueLinesByIds(List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) return Collections.emptyList();

        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;

        List<Tuple> rows = queryFactory
            .select(
                line.freightLineId,
                line.customerCode,
                line.financialDocType,
                line.financialDocumentId,
                line.taxNo,
                line.slipNo
            )
            .from(line)
            .where(line.freightLineId.in(lineIds))
            .fetch();

        return rows.stream()
            .map(t -> new FreightLineIssueSnapshot(
                t.get(line.freightLineId),
                t.get(line.customerCode),
                t.get(line.financialDocType),
                t.get(line.financialDocumentId),
                t.get(line.taxNo),
                t.get(line.slipNo)
            ))
            .toList();
    }

    // ── 서류 상태 집계 ────────────────────────────────────────────────────────

    /**
     * 서류별 tax_no/slip_no 존재 여부 집계. Tuple projection(S6).
     * hasTax = 서류에 연결된 라인 중 tax_no IS NOT NULL인 행 ≥ 1.
     * hasSlip = 서류에 연결된 라인 중 slip_no IS NOT NULL인 행 ≥ 1.
     * 서류 상태는 financial_document에서 직접 조회.
     */
    public List<DocumentLineFlag> loadDocumentTaxSlipFlags(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) return Collections.emptyList();

        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;

        // tax/slip 존재 여부를 MAX(CASE WHEN ... THEN 1 ELSE 0 END) 집계로 계산
        NumberExpression<Integer> hasTaxExpr =
            Expressions.numberTemplate(Integer.class,
                "max(case when {0} is not null then 1 else 0 end)", line.taxNo);
        NumberExpression<Integer> hasSlipExpr =
            Expressions.numberTemplate(Integer.class,
                "max(case when {0} is not null then 1 else 0 end)", line.slipNo);

        List<Tuple> rows = queryFactory
            .select(
                line.financialDocumentId,
                hasTaxExpr,
                hasSlipExpr,
                doc.documentStatus
            )
            .from(line)
            .join(doc).on(doc.financialDocumentId.eq(line.financialDocumentId))
            .where(line.financialDocumentId.in(documentIds))
            .groupBy(line.financialDocumentId, doc.documentStatus)
            .fetch();

        return rows.stream()
            .map(t -> new DocumentLineFlag(
                t.get(line.financialDocumentId),
                Integer.valueOf(1).equals(t.get(hasTaxExpr)),
                Integer.valueOf(1).equals(t.get(hasSlipExpr)),
                t.get(doc.documentStatus)
            ))
            .toList();
    }

    // ── 벌크 UPDATE ───────────────────────────────────────────────────────────

    /**
     * 지정 라인들의 tax_no·tax_dt를 일괄 기록한다.
     * .execute() 즉시 반영 — 이후 loadDocumentTaxSlipFlags DB 재조회 보장(S6).
     */
    public void bulkUpdateLineTax(List<Long> lineIds, String taxNo, String taxDt) {
        if (lineIds == null || lineIds.isEmpty()) return;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .set(line.taxNo, taxNo)
            .set(line.taxDt, taxDt)
            .where(line.freightLineId.in(lineIds))
            .execute();
    }

    /**
     * 지정 라인들의 slip_no·slip_dt를 일괄 기록한다.
     * .execute() 즉시 반영 — 이후 loadDocumentTaxSlipFlags DB 재조회 보장(S6).
     */
    public void bulkUpdateLineSlip(List<Long> lineIds, String slipNo, String slipDt) {
        if (lineIds == null || lineIds.isEmpty()) return;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .set(line.slipNo, slipNo)
            .set(line.slipDt, slipDt)
            .where(line.freightLineId.in(lineIds))
            .execute();
    }

    // ── WHERE 절 조합 ─────────────────────────────────────────────────────────

    private BooleanExpression[] buildWhere(
            SearchFreightLineCriteria c,
            QBmsFreightLineJpaEntity line,
            QHouseBlRefJpaEntity houseBl,
            QMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            // financial_document_id IS NOT NULL — inner join으로 자동 보장
            eqString(line.customerCode, c.customerCode()),
            eqString(line.financialDocType, c.financialDocType()),
            eqString(coalesce(houseBl.jobDiv, masterBl.jobDiv), c.jobDiv()),
            eqString(coalesce(houseBl.bound, masterBl.bound), c.bound()),
            dateFrom(line.performanceDt, c.performanceDtFrom()),
            dateTo(line.performanceDt, c.performanceDtTo()),
            issuedStatusFilter(line, c.issuedStatus())
        };
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private BooleanExpression eqString(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.eq(value);
    }

    private BooleanExpression eqString(
            com.querydsl.core.types.dsl.StringExpression expr, String value) {
        return (value == null || value.isBlank()) ? null : expr.eq(value);
    }

    private BooleanExpression dateFrom(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.goe(value);
    }

    private BooleanExpression dateTo(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.loe(value);
    }

    private com.querydsl.core.types.dsl.StringExpression coalesce(
            StringPath houseVal, StringPath masterVal) {
        return Expressions.stringTemplate("coalesce({0}, {1})", houseVal, masterVal);
    }

    /**
     * issuedStatus 필터. "Y"=발급 완료(taxNo/slipNo IS NOT NULL), "N"=미발급, 그 외=무시.
     * tax/slip 둘 중 하나라도 있으면 "Y"(OR 조건).
     */
    private BooleanExpression issuedStatusFilter(
            QBmsFreightLineJpaEntity line, String issuedStatus) {
        if (issuedStatus == null || issuedStatus.isBlank()) return null;
        return switch (issuedStatus.toUpperCase()) {
            case "Y" -> line.taxNo.isNotNull().or(line.slipNo.isNotNull());
            case "N" -> line.taxNo.isNull().and(line.slipNo.isNull());
            default -> null;
        };
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private FreightLineIssueRowView toRowView(
            Tuple t,
            QBmsFreightLineJpaEntity line,
            QFreightHeaderRefJpaEntity header,
            QFinancialDocumentJpaEntity doc,
            QHouseBlRefJpaEntity houseBl,
            QMasterBlRefJpaEntity masterBl) {

        String blType = t.get(header.blType);
        boolean isHouse = "HOUSE".equals(blType);

        String blNo = isHouse ? t.get(houseBl.hblNo) : t.get(masterBl.mblNo);
        String jobDiv = isHouse ? t.get(houseBl.jobDiv) : t.get(masterBl.jobDiv);
        String bound = isHouse ? t.get(houseBl.bound) : t.get(masterBl.bound);
        String etd = isHouse ? t.get(houseBl.etd) : t.get(masterBl.etd);

        return new FreightLineIssueRowView(
            t.get(line.freightLineId),
            t.get(line.freightHeaderId),
            blType,
            t.get(header.blId),
            blNo,
            jobDiv,
            bound,
            etd,
            t.get(line.freightType),
            t.get(line.financialDocType),
            t.get(line.freightCode),
            t.get(line.customerCode),
            "",   // customerName: Service에서 resolve
            t.get(line.currency),
            nvl(t.get(line.settleAmount)),
            nvl(t.get(line.localAmount)),
            nvl(t.get(line.settleTaxAmount)),
            nvl(t.get(line.localTaxAmount)),
            nvl(t.get(line.usdAmount)),
            t.get(line.performanceDt),
            t.get(line.financialDocumentId),
            t.get(doc.documentNo),
            t.get(doc.documentStatus),
            t.get(line.taxNo),
            t.get(line.taxDt),
            t.get(line.slipNo),
            t.get(line.slipDt)
        );
    }
}
