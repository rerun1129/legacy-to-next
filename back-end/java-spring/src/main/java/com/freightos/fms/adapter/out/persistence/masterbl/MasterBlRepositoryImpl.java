package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.freightos.common.util.Nullables;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MasterBlRepositoryImpl implements MasterBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<MasterBlSummaryResult> searchByFilter(MasterBlFilter filter, PageRequest pageRequest) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;

        List<MasterBlSummaryResult> content = queryFactory
            .select(Projections.constructor(MasterBlSummaryResult.class,
                m.masterBlId,
                m.mblNo,
                m.masterRefNo,
                m.jobDiv.stringValue(),
                m.bound.stringValue(),
                m.shipperCode,
                m.consigneeCode,
                m.polCode,
                m.podCode,
                m.etd,
                m.eta,
                m.operatorCode,
                m.createdAt
            ))
            .from(m)
            .where(
                m.bound.eq(filter.bound()),
                containsIgnoreCase(m.mblNo, filter.mblNo()),
                containsIgnoreCase(m.shipperCode, filter.shipperCode()),
                containsIgnoreCase(m.consigneeCode, filter.consigneeCode()),
                eqString(m.polCode, filter.polCode()),
                eqString(m.podCode, filter.podCode()),
                Nullables.mapIfHasText(filter.etdFrom(), m.etd::goe),
                Nullables.mapIfHasText(filter.etdTo(),   m.etd::loe)
            )
            .orderBy(m.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(m.count())
            .from(m)
            .where(
                m.bound.eq(filter.bound()),
                containsIgnoreCase(m.mblNo, filter.mblNo()),
                containsIgnoreCase(m.shipperCode, filter.shipperCode()),
                containsIgnoreCase(m.consigneeCode, filter.consigneeCode()),
                eqString(m.polCode, filter.polCode()),
                eqString(m.podCode, filter.podCode()),
                Nullables.mapIfHasText(filter.etdFrom(), m.etd::goe),
                Nullables.mapIfHasText(filter.etdTo(),   m.etd::loe)
            )
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }

    @Override
    public List<Long> findMasterBlKeysByMblNoExact(String mblNo) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;
        return queryFactory
                .select(m.masterBlId)
                .from(m)
                .where(m.mblNo.eq(mblNo))
                .orderBy(m.createdAt.desc(), m.masterBlId.desc())
                .limit(2)
                .fetch();
    }

    private static BooleanExpression containsIgnoreCase(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::containsIgnoreCase);
    }

    private static BooleanExpression eqString(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::eq);
    }
}
