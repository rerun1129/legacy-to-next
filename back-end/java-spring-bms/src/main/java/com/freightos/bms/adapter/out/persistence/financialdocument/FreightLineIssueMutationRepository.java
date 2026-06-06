package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 운임 행 발급/취소 벌크 UPDATE 전용 레포지토리.
 * .execute() 즉시 반영 — 이후 loadDocumentTaxSlipFlags DB 재조회 보장(S6).
 */
@Repository
@RequiredArgsConstructor
public class FreightLineIssueMutationRepository {

    private final JPAQueryFactory queryFactory;

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

    /**
     * 지정 라인들의 tax_no·tax_dt를 NULL로 일괄 클리어한다.
     * .execute() 즉시 반영 — 이후 loadDocumentTaxSlipFlags DB 재조회 보장(S6).
     */
    public void bulkClearLineTax(List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) return;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .setNull(line.taxNo)
            .setNull(line.taxDt)
            .where(line.freightLineId.in(lineIds))
            .execute();
    }

    /**
     * 지정 라인들의 slip_no·slip_dt를 NULL로 일괄 클리어한다.
     * .execute() 즉시 반영 — 이후 loadDocumentTaxSlipFlags DB 재조회 보장(S6).
     */
    public void bulkClearLineSlip(List<Long> lineIds) {
        if (lineIds == null || lineIds.isEmpty()) return;
        QBmsFreightLineJpaEntity line = QBmsFreightLineJpaEntity.bmsFreightLineJpaEntity;
        queryFactory
            .update(line)
            .setNull(line.slipNo)
            .setNull(line.slipDt)
            .where(line.freightLineId.in(lineIds))
            .execute();
    }
}
