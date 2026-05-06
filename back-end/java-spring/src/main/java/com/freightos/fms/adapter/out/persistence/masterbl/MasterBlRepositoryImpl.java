package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MasterBlRepositoryImpl implements MasterBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<MasterBlSummaryResult> searchByFilter(MasterBlFilter filter, PageRequest pageRequest) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;

        BooleanBuilder where = new BooleanBuilder();
        where.and(m.bound.eq(filter.bound()));

        if (StringUtils.hasText(filter.mblNo())) {
            where.and(m.mblNo.containsIgnoreCase(filter.mblNo()));
        }
        if (StringUtils.hasText(filter.shipperCode())) {
            where.and(m.shipperCode.containsIgnoreCase(filter.shipperCode()));
        }
        if (StringUtils.hasText(filter.consigneeCode())) {
            where.and(m.consigneeCode.containsIgnoreCase(filter.consigneeCode()));
        }
        if (StringUtils.hasText(filter.polCode())) {
            where.and(m.polCode.eq(filter.polCode()));
        }
        if (StringUtils.hasText(filter.podCode())) {
            where.and(m.podCode.eq(filter.podCode()));
        }
        if (StringUtils.hasText(filter.etdFrom())) {
            where.and(m.etd.goe(filter.etdFrom()));
        }
        if (StringUtils.hasText(filter.etdTo())) {
            where.and(m.etd.loe(filter.etdTo()));
        }

        List<MasterBlSummaryResult> content = queryFactory
            .select(Projections.constructor(MasterBlSummaryResult.class,
                m.masterBlId,
                m.mblNo,
                m.masterRefNo,
                m.jobDiv,
                m.bound,
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
            .where(where)
            .orderBy(m.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(m.count())
            .from(m)
            .where(where)
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }
}
