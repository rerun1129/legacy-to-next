package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HouseBlRepositoryImpl implements HouseBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<HouseBlSummary> findSummariesByJobDivAndBound(
            JobDiv jobDiv, Bound bound, PageRequest pageRequest) {

        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;

        List<HouseBlSummary> content = queryFactory
            .select(Projections.constructor(HouseBlSummary.class,
                h.houseBlId,
                h.hblNo,
                h.jobDiv,
                h.bound,
                h.polCode,
                h.podCode,
                h.etd,
                h.eta,
                h.shipperCode,
                h.consigneeCode,
                h.pkgQty,
                h.pkgUnit,
                h.createdAt
            ))
            .from(h)
            .where(h.jobDiv.eq(jobDiv).and(h.bound.eq(bound)))
            .orderBy(h.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(h.count())
            .from(h)
            .where(h.jobDiv.eq(jobDiv).and(h.bound.eq(bound)))
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }
}
