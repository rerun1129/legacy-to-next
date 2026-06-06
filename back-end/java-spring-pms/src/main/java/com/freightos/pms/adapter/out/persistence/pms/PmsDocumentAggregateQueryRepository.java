package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.adapter.out.persistence.entity.QPmsFinancialDocumentRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightHeaderRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsFreightLineRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsMasterBlRefJpaEntity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * financial_document 기반 B/L 집계 쿼리 레포지토리 (DOCUMENT_CREATED basis).
 *
 * fan-out 방지 전략:
 * 1. count + 페이지 B/L 키 조회 → distinct (bl_type, bl_id) 페이지
 * 2. 해당 B/L 키 목록에서 DISTINCT (bl_type, bl_id, doc_id, doc_type, local_total, usd_total) 조회
 * 3. 서비스가 아닌 이 레포지토리에서 in-memory fold (doc_type별 합산)
 * 이렇게 하면 financial_document→freight_line 1:N fan-out으로 인한 금액 중복 합산 방지.
 */
@Repository
@RequiredArgsConstructor
public class PmsDocumentAggregateQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PmsPerformanceWhereBuilder whereBuilder;

    public Page<PmsRawBlRow> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        QPmsFinancialDocumentRefJpaEntity doc = QPmsFinancialDocumentRefJpaEntity.pmsFinancialDocumentRefJpaEntity;
        QPmsFreightHeaderRefJpaEntity header = QPmsFreightHeaderRefJpaEntity.pmsFreightHeaderRefJpaEntity;
        QPmsFreightLineRefJpaEntity line = QPmsFreightLineRefJpaEntity.pmsFreightLineRefJpaEntity;
        QPmsHouseBlRefJpaEntity houseBl = QPmsHouseBlRefJpaEntity.pmsHouseBlRefJpaEntity;
        QPmsMasterBlRefJpaEntity masterBl = QPmsMasterBlRefJpaEntity.pmsMasterBlRefJpaEntity;

        BooleanExpression[] where = whereBuilder.buildForDocument(command, doc, header, houseBl, masterBl);

        // Step 1: count distinct B/L 키
        NumberExpression<Long> countExpr = Expressions.numberTemplate(Long.class,
            "count(distinct ({0},{1}))", header.blType, header.blId);
        Long countResult = queryFactory
            .select(countExpr)
            .from(doc)
            .join(line).on(line.financialDocumentId.eq(doc.financialDocumentId))
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .fetchOne();
        long total = countResult != null ? countResult : 0L;
        if (total == 0L) return new PageImpl<>(List.of(), pageable, 0L);

        // Step 2: 페이지 B/L 키 목록 조회 (identity 컬럼은 max로 대표)
        List<Tuple> blKeyRows = queryFactory
            .select(
                header.blType, header.blId,
                houseBl.hblNo.max(), houseBl.mblNo.max(), masterBl.mblNo.max(),
                houseBl.jobDiv.max(), masterBl.jobDiv.max(),
                houseBl.bound.max(), masterBl.bound.max(),
                houseBl.etd.max(), masterBl.etd.max(),
                houseBl.eta.max(), masterBl.eta.max(),
                doc.performanceDt.max(),
                header.actualCustomerCode.max(), header.settlePartnerCode.max(), header.linerCode.max(),
                houseBl.polCode.max(), masterBl.polCode.max(),
                houseBl.podCode.max(), masterBl.podCode.max(),
                houseBl.salesManCode.max(), houseBl.incoterms.max(),
                houseBl.houseBlId.max(),
                doc.teamCode.max(), doc.operator.max()
            )
            .from(doc)
            .join(line).on(line.financialDocumentId.eq(doc.financialDocumentId))
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .leftJoin(houseBl).on(header.blType.eq("HOUSE").and(header.blId.eq(houseBl.houseBlId)))
            .leftJoin(masterBl).on(header.blType.eq("MASTER").and(header.blId.eq(masterBl.masterBlId)))
            .where(where)
            .groupBy(header.blType, header.blId)
            .orderBy(doc.performanceDt.max().desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (blKeyRows.isEmpty()) return new PageImpl<>(List.of(), pageable, total);

        // Step 3: 해당 B/L 키에 대해 DISTINCT 서류 금액 조회 (fan-out 방지)
        List<String> blTypes = blKeyRows.stream().map(r -> r.get(header.blType)).toList();
        List<Long> blIds = blKeyRows.stream().map(r -> r.get(header.blId)).toList();

        List<Tuple> docAmountRows = queryFactory
            .selectDistinct(
                header.blType, header.blId,
                doc.financialDocumentId,
                doc.documentType,
                doc.localTotalAmount,
                doc.usdTotalAmount
            )
            .from(doc)
            .join(line).on(line.financialDocumentId.eq(doc.financialDocumentId))
            .join(header).on(header.freightHeaderId.eq(line.freightHeaderId))
            .where(header.blType.in(blTypes), header.blId.in(blIds))
            .fetch();

        // Step 4: in-memory fold
        Map<String, Map<String, BigDecimal>> localByBlKey = new HashMap<>();
        Map<String, Map<String, BigDecimal>> usdByBlKey = new HashMap<>();
        for (Tuple t : docAmountRows) {
            String key = t.get(header.blType) + ":" + t.get(header.blId);
            String docType = t.get(doc.documentType);
            BigDecimal local = t.get(doc.localTotalAmount);
            BigDecimal usd = t.get(doc.usdTotalAmount);
            localByBlKey.computeIfAbsent(key, k -> new HashMap<>())
                .merge(docType, nvl(local), BigDecimal::add);
            usdByBlKey.computeIfAbsent(key, k -> new HashMap<>())
                .merge(docType, nvl(usd), BigDecimal::add);
        }

        List<PmsRawBlRow> content = new ArrayList<>();
        for (Tuple t : blKeyRows) {
            String blType = t.get(header.blType);
            boolean isHouse = "HOUSE".equals(blType);
            String key = blType + ":" + t.get(header.blId);
            Map<String, BigDecimal> localMap = localByBlKey.getOrDefault(key, Map.of());
            Map<String, BigDecimal> usdMap = usdByBlKey.getOrDefault(key, Map.of());

            content.add(new PmsRawBlRow(
                blType, t.get(header.blId),
                isHouse ? t.get(houseBl.hblNo.max()) : null,
                isHouse ? t.get(houseBl.mblNo.max()) : t.get(masterBl.mblNo.max()),
                isHouse ? t.get(houseBl.jobDiv.max()) : t.get(masterBl.jobDiv.max()),
                isHouse ? t.get(houseBl.bound.max()) : t.get(masterBl.bound.max()),
                isHouse ? t.get(houseBl.etd.max()) : t.get(masterBl.etd.max()),
                isHouse ? t.get(houseBl.eta.max()) : t.get(masterBl.eta.max()),
                t.get(doc.performanceDt.max()),
                t.get(header.actualCustomerCode.max()),
                t.get(header.settlePartnerCode.max()),
                t.get(header.linerCode.max()),
                isHouse ? t.get(houseBl.polCode.max()) : t.get(masterBl.polCode.max()),
                isHouse ? t.get(houseBl.podCode.max()) : t.get(masterBl.podCode.max()),
                isHouse ? t.get(houseBl.salesManCode.max()) : null,
                isHouse ? t.get(houseBl.incoterms.max()) : null,
                isHouse ? t.get(houseBl.houseBlId.max()) : null,
                t.get(doc.teamCode.max()),
                t.get(doc.operator.max()),
                localMap.get("INVOICE"), localMap.get("DEBIT"),
                localMap.get("PAYMENT"), localMap.get("CREDIT"),
                usdMap.get("INVOICE"), usdMap.get("DEBIT"),
                usdMap.get("PAYMENT"), usdMap.get("CREDIT")
            ));
        }

        return new PageImpl<>(content, pageable, total);
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
