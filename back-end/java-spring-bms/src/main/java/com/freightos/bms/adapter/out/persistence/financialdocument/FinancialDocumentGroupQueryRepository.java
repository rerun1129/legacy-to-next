package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.GroupDocumentSnapshot;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * 금융 서류 그룹 부여/해제 전용 QueryDSL 레포지토리.
 * 벌크 UPDATE와 그룹 처리용 스냅샷 조회를 담당한다.
 * FreightLineQueryRepository의 bulkLinkLines/bulkUnlinkLines 패턴 복제.
 */
@Repository
@RequiredArgsConstructor
public class FinancialDocumentGroupQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * ids에 해당하는 서류의 그룹 처리용 스냅샷을 로드한다.
     */
    public List<GroupDocumentSnapshot> loadGroupSnapshots(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;

        return queryFactory
            .select(doc)
            .from(doc)
            .where(doc.financialDocumentId.in(ids))
            .fetch()
            .stream()
            .map(e -> new GroupDocumentSnapshot(
                e.getFinancialDocumentId(),
                e.getCustomerCode(),
                e.getDocumentType(),
                e.getDocumentStatus(),
                e.getGroupFinancialNo(),
                e.getDocumentDt()
            ))
            .toList();
    }

    /**
     * 지정 서류들에 그룹 번호를 일괄 부여한다.
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass.
     */
    public void bulkAssignGroupNo(List<Long> ids, String groupNo) {
        if (ids == null || ids.isEmpty()) return;

        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;
        queryFactory
            .update(doc)
            .set(doc.groupFinancialNo, groupNo)
            .where(doc.financialDocumentId.in(ids))
            .execute();
    }

    /**
     * 지정 서류들의 그룹 번호를 null로 일괄 해제한다.
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass.
     */
    public void bulkClearGroupNo(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;
        queryFactory
            .update(doc)
            .setNull(doc.groupFinancialNo)
            .where(doc.financialDocumentId.in(ids))
            .execute();
    }

    /**
     * 지정 서류들의 document_status를 일괄 갱신한다.
     * 그룹 부여 시 GROUPED 승급, 해제 시 CREATED 강등에 사용.
     * QueryDSL 벌크 UPDATE — 영속 컨텍스트 bypass.
     */
    public void bulkUpdateDocumentStatus(List<Long> ids, String status) {
        if (ids == null || ids.isEmpty()) return;

        QFinancialDocumentJpaEntity doc = QFinancialDocumentJpaEntity.financialDocumentJpaEntity;
        queryFactory
            .update(doc)
            .set(doc.documentStatus, status)
            .where(doc.financialDocumentId.in(ids))
            .execute();
    }
}
