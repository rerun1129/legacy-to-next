package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.adapter.out.persistence.codename.entity.QHouseBlRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QMasterBlRefJpaEntity;
import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.SearchFinancialDocumentCriteria;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
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
 * 금융 서류 전역 검색 전용 QueryDSL 레포지토리.
 * cross-schema(fms.house_bl / fms.master_bl) leftJoin을 포함하며,
 * B/L 파생 필드(jobDiv·bound·blNo·etd·eta)가 WHERE 조건에 쓰이므로 조인이 불가피함.
 * 이름/파생 필드 resolve는 FinancialDocumentQueryService에서 CodeNameResolver로 처리.
 */
@Repository
@RequiredArgsConstructor
public class FinancialDocumentSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 금융 서류 전역 검색.
     * documentTypes IN 필수, 나머지 조건은 blank/null 시 무시.
     * doc(financial_document) 기준, 대표 freight_line → freight_header 조인으로 blType·blId 확보.
     * leftJoin house_bl/master_bl(blType 분기)으로 jobDiv·bound·blNo·etd·eta 파생.
     * orderBy document_no desc. selectDistinct 서류 단위.
     * customerName·teamName·operatorName은 빈 문자열로 반환(Service에서 resolve).
     */
    public Page<FinancialDocumentSearchView> search(
            SearchFinancialDocumentCriteria criteria, Pageable pageable) {
        if (criteria.documentTypes() == null || criteria.documentTypes().isEmpty()) {
            return Page.empty(pageable);
        }

        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        QFreightHeaderRefJpaEntity header = QFreightHeaderRefJpaEntity.freightHeaderRefJpaEntity;
        QHouseBlRefJpaEntity houseBl = QHouseBlRefJpaEntity.houseBlRefJpaEntity;
        QMasterBlRefJpaEntity masterBl = QMasterBlRefJpaEntity.masterBlRefJpaEntity;

        BooleanExpression[] whereConditions = buildWhere(criteria, doc, houseBl, masterBl);

        // count 쿼리 — DISTINCT document_id 수
        Long countResult = queryFactory
            .select(doc.financialDocumentId.countDistinct())
            .from(doc)
            .join(line).on(line.financialDocumentId.eq(doc.financialDocumentId))
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(
                header.blType.eq("HOUSE")
                    .and(header.blId.eq(houseBl.houseBlId))
            )
            .leftJoin(masterBl).on(
                header.blType.eq("MASTER")
                    .and(header.blId.eq(masterBl.masterBlId))
            )
            .where(whereConditions)
            .fetchOne();
        long total = countResult != null ? countResult : 0L;

        // content 쿼리
        List<Tuple> rows = queryFactory
            .selectDistinct(
                doc.financialDocumentId,
                doc.documentNo,
                doc.documentType,
                doc.documentDt,
                doc.documentStatus,
                doc.customerCode,
                doc.settleTotalAmount,
                doc.localTotalAmount,
                doc.settleTotalVat,
                doc.localTotalVat,
                doc.usdTotalAmount,
                doc.performanceDt,
                doc.teamCode,
                doc.operator,
                doc.groupFinancialNo,
                header.blType,
                header.blId,
                houseBl.jobDiv,
                houseBl.bound,
                houseBl.hblNo,
                houseBl.etd,
                houseBl.eta,
                masterBl.jobDiv,
                masterBl.bound,
                masterBl.mblNo,
                masterBl.etd,
                masterBl.eta
            )
            .from(doc)
            .join(line).on(line.financialDocumentId.eq(doc.financialDocumentId))
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(
                header.blType.eq("HOUSE")
                    .and(header.blId.eq(houseBl.houseBlId))
            )
            .leftJoin(masterBl).on(
                header.blType.eq("MASTER")
                    .and(header.blId.eq(masterBl.masterBlId))
            )
            .where(whereConditions)
            .orderBy(doc.documentNo.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<FinancialDocumentSearchView> content = rows.stream()
            .map(t -> toSearchView(t, doc, header, houseBl, masterBl))
            .toList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 특정 서류에 속한 운임 라인 전 컬럼 조회.
     * freightName·customerName은 빈 문자열로 반환(Service에서 resolve).
     */
    public List<FreightLineDetailView> findLinesByDocument(Long documentId) {
        if (documentId == null) {
            return Collections.emptyList();
        }
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;

        List<Tuple> rows = queryFactory
            .select(
                line.freightLineId,
                line.freightHeaderId,
                line.freightType,
                line.financialDocType,
                line.freightCode,
                line.unitQuantity,
                line.unitPrice,
                line.per,
                line.currency,
                line.exchangeRate,
                line.settleAmount,
                line.localAmount,
                line.settleTaxAmount,
                line.localTaxAmount,
                line.usdExchangeRate,
                line.usdAmount,
                line.customerCode,
                line.taxType,
                line.taxNo,
                line.taxDt,
                line.slipNo,
                line.slipDt,
                line.performanceDt,
                line.financialDocumentId
            )
            .from(line)
            .where(line.financialDocumentId.eq(documentId))
            .fetch();

        return rows.stream().map(t -> new FreightLineDetailView(
            t.get(line.freightLineId),
            t.get(line.freightHeaderId),
            t.get(line.freightType),
            t.get(line.financialDocType),
            t.get(line.freightCode),
            "",   // freightName: Service에서 resolve
            t.get(line.unitQuantity),
            t.get(line.unitPrice),
            t.get(line.per),
            t.get(line.currency),
            t.get(line.exchangeRate),
            nvl(t.get(line.settleAmount)),
            nvl(t.get(line.localAmount)),
            nvl(t.get(line.settleTaxAmount)),
            nvl(t.get(line.localTaxAmount)),
            t.get(line.usdExchangeRate),
            nvl(t.get(line.usdAmount)),
            t.get(line.customerCode),
            "",   // customerName: Service에서 resolve
            t.get(line.taxType),
            t.get(line.taxNo),
            t.get(line.taxDt),
            t.get(line.slipNo),
            t.get(line.slipDt),
            t.get(line.performanceDt),
            t.get(line.financialDocumentId)
        )).toList();
    }

    // ── WHERE 절 조합 ─────────────────────────────────────────────────────────

    private BooleanExpression[] buildWhere(
            SearchFinancialDocumentCriteria c,
            QFinancialDocumentJpaEntity doc,
            QHouseBlRefJpaEntity houseBl,
            QMasterBlRefJpaEntity masterBl) {

        return new BooleanExpression[] {
            doc.documentType.in(c.documentTypes()),
            eqString(doc.documentStatus, c.documentStatus()),
            eqString(doc.customerCode, c.customerCode()),
            likeDocumentNo(doc.documentNo, c.documentNoLike()),
            eqString(doc.teamCode, c.teamCode()),
            eqString(doc.operator, c.operator()),
            dateFrom(doc.documentDt, c.documentDtFrom()),
            dateTo(doc.documentDt, c.documentDtTo()),
            dateFrom(doc.performanceDt, c.performanceDtFrom()),
            dateTo(doc.performanceDt, c.performanceDtTo()),
            dateFrom(coalesce(houseBl.etd, masterBl.etd), c.etdFrom()),
            dateTo(coalesce(houseBl.etd, masterBl.etd), c.etdTo()),
            dateFrom(coalesce(houseBl.eta, masterBl.eta), c.etaFrom()),
            dateTo(coalesce(houseBl.eta, masterBl.eta), c.etaTo()),
            eqString(coalesce(houseBl.jobDiv, masterBl.jobDiv), c.jobDiv()),
            eqString(coalesce(houseBl.bound, masterBl.bound), c.bound())
        };
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private BooleanExpression eqString(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.eq(value);
    }

    private BooleanExpression eqString(StringExpression expr, String value) {
        return (value == null || value.isBlank()) ? null : expr.eq(value);
    }

    private BooleanExpression likeDocumentNo(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.containsIgnoreCase(value);
    }

    private BooleanExpression dateFrom(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.goe(value);
    }

    private BooleanExpression dateTo(StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.loe(value);
    }

    private BooleanExpression dateFrom(StringExpression expr, String value) {
        return (value == null || value.isBlank()) ? null : expr.goe(value);
    }

    private BooleanExpression dateTo(StringExpression expr, String value) {
        return (value == null || value.isBlank()) ? null : expr.loe(value);
    }

    /** house/master 중 non-null 값을 반환하는 COALESCE 표현식 헬퍼. */
    private StringExpression coalesce(StringPath houseVal, StringPath masterVal) {
        return Expressions.stringTemplate("coalesce({0}, {1})", houseVal, masterVal);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private FinancialDocumentSearchView toSearchView(
            Tuple t,
            QFinancialDocumentJpaEntity doc,
            QFreightHeaderRefJpaEntity header,
            QHouseBlRefJpaEntity houseBl,
            QMasterBlRefJpaEntity masterBl) {

        String blType = t.get(header.blType);
        boolean isHouse = "HOUSE".equals(blType);

        String jobDiv = isHouse ? t.get(houseBl.jobDiv)  : t.get(masterBl.jobDiv);
        String bound  = isHouse ? t.get(houseBl.bound)   : t.get(masterBl.bound);
        String blNo   = isHouse ? t.get(houseBl.hblNo)   : t.get(masterBl.mblNo);
        String etd    = isHouse ? t.get(houseBl.etd)     : t.get(masterBl.etd);
        String eta    = isHouse ? t.get(houseBl.eta)     : t.get(masterBl.eta);

        return new FinancialDocumentSearchView(
            t.get(doc.financialDocumentId),
            t.get(doc.documentNo),
            t.get(doc.documentType),
            t.get(doc.documentDt),
            t.get(doc.documentStatus),
            t.get(doc.customerCode),
            "",   // customerName: Service에서 resolve
            nvl(t.get(doc.settleTotalAmount)),
            nvl(t.get(doc.localTotalAmount)),
            nvl(t.get(doc.settleTotalVat)),
            nvl(t.get(doc.localTotalVat)),
            nvl(t.get(doc.usdTotalAmount)),
            t.get(doc.performanceDt),
            t.get(doc.teamCode),
            "",   // teamName: Service에서 resolve
            t.get(doc.operator),
            "",   // operatorName: Service에서 resolve
            t.get(doc.groupFinancialNo),
            blType,
            t.get(header.blId),
            jobDiv,
            bound,
            blNo,
            etd,
            eta
        );
    }
}
