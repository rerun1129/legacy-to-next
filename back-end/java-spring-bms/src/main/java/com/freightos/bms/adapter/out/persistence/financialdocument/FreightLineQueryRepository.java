package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.application.financialdocument.port.out.FreightLineSnapshot;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 금융 서류 발행 관련 QueryDSL 조회 레포지토리.
 * ①라인 스냅샷 로드 ②발행 가능 라인 조회 ③B/L 서류 목록 조회.
 */
@Repository
@RequiredArgsConstructor
public class FreightLineQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * lineIds에 해당하는 운임 라인 스냅샷을 로드한다.
     * financialDocumentId 포함 — 이미 발행된 라인 검증에 사용.
     */
    List<FreightLineSnapshot> loadLinesByIds(List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) return Collections.emptyList();

        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        BooleanExpression inIds = line.freightLineId.in(lineIds);

        return queryFactory
            .select(line)
            .from(line)
            .where(inIds)
            .fetch()
            .stream()
            .map(this::toSnapshot)
            .toList();
    }

    /**
     * 특정 headerId·freightType의 운임 라인 목록(발행 여부 포함).
     * financial_document LEFT JOIN으로 발행된 라인의 document_no를 동반.
     * 미발행 라인은 financialDocumentId·documentNo = null.
     */
    List<IssuableLineView> findIssuableLines(Long headerId, String freightType) {
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;

        BooleanExpression headerFilter = line.freightHeaderId.eq(headerId);
        BooleanExpression typeFilter = eqString(line.freightType, freightType);

        List<Tuple> rows = queryFactory
            .select(
                line.freightLineId,
                line.freightType,
                line.financialDocType,
                line.freightCode,
                line.customerCode,
                line.currency,
                line.settleAmount,
                line.localAmount,
                line.settleTaxAmount,
                line.localTaxAmount,
                line.usdAmount,
                line.performanceDt,
                line.financialDocumentId,
                doc.documentNo
            )
            .from(line)
            .leftJoin(doc).on(doc.financialDocumentId.eq(line.financialDocumentId))
            .where(headerFilter, typeFilter)
            .fetch();

        return rows.stream().map(t -> new IssuableLineView(
            t.get(line.freightLineId),
            t.get(line.freightType),
            t.get(line.financialDocType),
            t.get(line.freightCode),
            t.get(line.customerCode),
            "",  // customerName은 Service에서 resolve
            t.get(line.currency),
            t.get(line.settleAmount),
            t.get(line.localAmount),
            t.get(line.settleTaxAmount),
            t.get(line.localTaxAmount),
            t.get(line.usdAmount),
            t.get(line.performanceDt),
            t.get(line.financialDocumentId),
            t.get(doc.documentNo)
        )).toList();
    }

    /**
     * B/L에 속한 금융 서류 목록 조회.
     * freight_header → freight_line → financial_document JOIN. DISTINCT 서류 기준.
     * customerName은 Service에서 resolve.
     */
    List<FinancialDocumentView> findDocumentsByBl(String blType, String blId) {
        QFreightHeaderRefJpaEntity header = QFreightHeaderRefJpaEntity.freightHeaderRefJpaEntity;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;

        BooleanExpression blTypeFilter = header.blType.eq(blType);
        BooleanExpression blIdFilter = header.blId.eq(blId);
        BooleanExpression notNull = line.financialDocumentId.isNotNull();

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
                doc.operator
            )
            .from(header)
            .join(line).on(line.freightHeaderId.eq(header.freightHeaderId))
            .join(doc).on(doc.financialDocumentId.eq(line.financialDocumentId))
            .where(blTypeFilter, blIdFilter, notNull)
            .fetch();

        return rows.stream().map(t -> new FinancialDocumentView(
            t.get(doc.financialDocumentId),
            t.get(doc.documentNo),
            t.get(doc.documentType),
            t.get(doc.documentDt),
            t.get(doc.documentStatus),
            t.get(doc.customerCode),
            "",  // customerName은 Service에서 resolve
            nvl(t.get(doc.settleTotalAmount)),
            nvl(t.get(doc.localTotalAmount)),
            nvl(t.get(doc.settleTotalVat)),
            nvl(t.get(doc.localTotalVat)),
            nvl(t.get(doc.usdTotalAmount)),
            t.get(doc.performanceDt),
            t.get(doc.teamCode),
            t.get(doc.operator)
        )).toList();
    }

    /**
     * 선택된 라인들에 서류 ID와 performance_dt를 일괄 연결한다(§6.15).
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass이므로 호출 전후 flush/clear 고려.
     */
    void bulkLinkLines(List<Long> lineIds, Long documentId, String performanceDt) {
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .set(line.financialDocumentId, documentId)
            .set(line.performanceDt, performanceDt)
            .where(line.freightLineId.in(lineIds))
            .execute();
    }

    /**
     * 서류에 연결된 모든 라인의 financial_document_id를 null로 해제한다.
     * performance_dt는 유지한다.
     */
    void bulkUnlinkLines(Long documentId) {
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .setNull(line.financialDocumentId)
            .where(line.financialDocumentId.eq(documentId))
            .execute();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private FreightLineSnapshot toSnapshot(BmsFreightLineJpaEntity e) {
        return new FreightLineSnapshot(
            e.getFreightLineId(),
            e.getFreightHeaderId(),
            e.getFreightType(),
            e.getFinancialDocType(),
            e.getCustomerCode(),
            e.getSettleAmount(),
            e.getLocalAmount(),
            e.getSettleTaxAmount(),
            e.getLocalTaxAmount(),
            e.getUsdAmount(),
            e.getFinancialDocumentId()
        );
    }

    private BooleanExpression eqString(
            com.querydsl.core.types.dsl.StringPath path, String value) {
        return (value == null || value.isBlank()) ? null : path.eq(value);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
