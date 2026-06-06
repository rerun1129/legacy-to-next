package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightHeaderRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightLineRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsMasterBlRefJpaEntity;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * freight_line 기반 B/L 집계 쿼리 레포지토리.
 * FREIGHT_INPUT / TAX_ISSUED / SLIP_ISSUED basis 공통.
 * GROUP BY (freight_header.bl_type, freight_header.bl_id).
 * 집계가 아닌 identity 컬럼은 Postgres GROUP BY 기능 의존 금지 → min/max 래핑.
 */
@Repository
@RequiredArgsConstructor
public class PmsFreightLineAggregateQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PmsPerformanceWhereBuilder whereBuilder;

    public Page<PmsRawBlRow> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        QPmsFreightLineRefJpaEntity line = QPmsFreightLineRefJpaEntity.pmsFreightLineRefJpaEntity;
        QPmsFreightHeaderRefJpaEntity header = QPmsFreightHeaderRefJpaEntity.pmsFreightHeaderRefJpaEntity;
        QPmsHouseBlRefJpaEntity houseBl = QPmsHouseBlRefJpaEntity.pmsHouseBlRefJpaEntity;
        QPmsMasterBlRefJpaEntity masterBl = QPmsMasterBlRefJpaEntity.pmsMasterBlRefJpaEntity;

        BooleanExpression[] where = buildWhere(command, line, header, houseBl, masterBl);

        // total count — count(distinct (bl_type, bl_id)) 복합 키 DISTINCT
        NumberExpression<Long> countExpr = Expressions.numberTemplate(Long.class,
            "count(distinct ({0},{1}))", header.blType, header.blId);

        Long countResult = queryFactory
            .select(countExpr)
            .from(line)
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .fetchOne();
        long total = countResult != null ? countResult : 0L;

        if (total == 0L) return new PageImpl<>(List.of(), pageable, 0L);

        // ── 집계 표현식 (SUM by doc_type, min/max for identity) ─────────────────
        NumberExpression<BigDecimal> invoiceLocal = sumWhenDocType(line, "INVOICE", false);
        NumberExpression<BigDecimal> debitLocal = sumWhenDocType(line, "DEBIT", false);
        NumberExpression<BigDecimal> paymentLocal = sumWhenDocType(line, "PAYMENT", false);
        NumberExpression<BigDecimal> creditLocal = sumWhenDocType(line, "CREDIT", false);
        NumberExpression<BigDecimal> invoiceUsd = sumWhenDocType(line, "INVOICE", true);
        NumberExpression<BigDecimal> debitUsd = sumWhenDocType(line, "DEBIT", true);
        NumberExpression<BigDecimal> paymentUsd = sumWhenDocType(line, "PAYMENT", true);
        NumberExpression<BigDecimal> creditUsd = sumWhenDocType(line, "CREDIT", true);

        List<Tuple> rows = queryFactory
            .select(
                header.blType, header.blId,
                houseBl.hblNo.min(), houseBl.mblNo.min(), masterBl.mblNo.min(),
                houseBl.jobDiv.min(), masterBl.jobDiv.min(),
                houseBl.bound.min(), masterBl.bound.min(),
                houseBl.etd.min(), masterBl.etd.min(),
                houseBl.eta.min(), masterBl.eta.min(),
                line.performanceDt.max(),
                header.actualCustomerCode.min(), header.settlePartnerCode.min(), header.linerCode.min(),
                houseBl.polCode.min(), masterBl.polCode.min(),
                houseBl.podCode.min(), masterBl.podCode.min(),
                houseBl.salesManCode.min(), houseBl.incoterms.min(),
                houseBl.houseBlId.min(),
                houseBl.teamCode.min(),
                invoiceLocal, debitLocal, paymentLocal, creditLocal,
                invoiceUsd, debitUsd, paymentUsd, creditUsd
            )
            .from(line)
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .groupBy(header.blType, header.blId)
            .orderBy(line.performanceDt.max().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<PmsRawBlRow> content = rows.stream()
            .map(t -> toRawRow(t, line, header, houseBl, masterBl,
                invoiceLocal, debitLocal, paymentLocal, creditLocal,
                invoiceUsd, debitUsd, paymentUsd, creditUsd))
            .toList();

        return new PageImpl<>(content, pageable, total);
    }

    // ── WHERE 절 ─────────────────────────────────────────────────────────────────

    private BooleanExpression[] buildWhere(
            SearchPmsPerformanceCommand c,
            QPmsFreightLineRefJpaEntity line,
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl) {

        BooleanExpression[] base = whereBuilder.buildForFreightLine(c, line, header, houseBl, masterBl);
        // basis 분기 조건 추가
        BooleanExpression basisCondition = switch (c.effectiveBasis()) {
            case TAX_ISSUED -> line.taxNo.isNotNull();
            case SLIP_ISSUED -> line.slipNo.isNotNull();
            default -> null; // FREIGHT_INPUT: 조건 없음
        };
        if (basisCondition == null) return base;
        BooleanExpression[] extended = new BooleanExpression[base.length + 1];
        System.arraycopy(base, 0, extended, 0, base.length);
        extended[base.length] = basisCondition;
        return extended;
    }

    // ── 집계 표현식 헬퍼 ───────────────────────────────────────────────────────────

    private NumberExpression<BigDecimal> sumWhenDocType(
            QPmsFreightLineRefJpaEntity line, String docType, boolean usd) {
        String col = usd ? "usd_amount" : "local_amount";
        // SUM(CASE WHEN financial_doc_type=? THEN col ELSE 0 END)
        return Expressions.numberTemplate(BigDecimal.class,
            "sum(case when {0} = '" + docType + "' then {1} else 0 end)",
            line.financialDocType,
            usd ? line.usdAmount : line.localAmount);
    }

    // ── 행 매핑 ─────────────────────────────────────────────────────────────────

    private PmsRawBlRow toRawRow(
            Tuple t,
            QPmsFreightLineRefJpaEntity line,
            QPmsFreightHeaderRefJpaEntity header,
            QPmsHouseBlRefJpaEntity houseBl,
            QPmsMasterBlRefJpaEntity masterBl,
            NumberExpression<BigDecimal> invoiceLocal,
            NumberExpression<BigDecimal> debitLocal,
            NumberExpression<BigDecimal> paymentLocal,
            NumberExpression<BigDecimal> creditLocal,
            NumberExpression<BigDecimal> invoiceUsd,
            NumberExpression<BigDecimal> debitUsd,
            NumberExpression<BigDecimal> paymentUsd,
            NumberExpression<BigDecimal> creditUsd) {

        String blType = t.get(header.blType);
        boolean isHouse = "HOUSE".equals(blType);

        String houseBlNo = isHouse ? t.get(houseBl.hblNo.min()) : null;
        String mblFromHouse = isHouse ? t.get(houseBl.mblNo.min()) : null;
        String mblFromMaster = !isHouse ? t.get(masterBl.mblNo.min()) : null;
        String masterBlNo = isHouse ? mblFromHouse : mblFromMaster;

        String jobDiv = isHouse ? t.get(houseBl.jobDiv.min()) : t.get(masterBl.jobDiv.min());
        String bound = isHouse ? t.get(houseBl.bound.min()) : t.get(masterBl.bound.min());
        String etd = isHouse ? t.get(houseBl.etd.min()) : t.get(masterBl.etd.min());
        String eta = isHouse ? t.get(houseBl.eta.min()) : t.get(masterBl.eta.min());
        String polCode = isHouse ? t.get(houseBl.polCode.min()) : t.get(masterBl.polCode.min());
        String podCode = isHouse ? t.get(houseBl.podCode.min()) : t.get(masterBl.podCode.min());

        return new PmsRawBlRow(
            blType, t.get(header.blId),
            houseBlNo, masterBlNo,
            jobDiv, bound, etd, eta,
            t.get(line.performanceDt.max()),
            t.get(header.actualCustomerCode.min()),
            t.get(header.settlePartnerCode.min()),
            t.get(header.linerCode.min()),
            polCode, podCode,
            isHouse ? t.get(houseBl.salesManCode.min()) : null,
            isHouse ? t.get(houseBl.incoterms.min()) : null,
            isHouse ? t.get(houseBl.houseBlId.min()) : null,
            isHouse ? t.get(houseBl.teamCode.min()) : null,
            null,  // operator — freight_line 기반 집계에서는 financial_document 미조인
            t.get(invoiceLocal), t.get(debitLocal), t.get(paymentLocal), t.get(creditLocal),
            t.get(invoiceUsd), t.get(debitUsd), t.get(paymentUsd), t.get(creditUsd)
        );
    }
}
