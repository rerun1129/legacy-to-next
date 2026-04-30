package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlAirJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlSeaJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
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

    @Override
    public List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlSeaJpaEntity sea = QHouseBlSeaJpaEntity.houseBlSeaJpaEntity;

        return queryFactory
            .select(Projections.constructor(ConsoledHouseBlSeaSummary.class,
                h.houseBlId,
                h.hblNo,
                h.shipperCode,
                h.consigneeCode,
                h.docPartnerCode,
                h.pkgQty,
                h.pkgUnit,
                h.grossWeightKg,
                h.cbm,
                h.etd,
                h.eta,
                sea.vesselName,
                sea.voyageNo,
                h.polCode,
                h.podCode
            ))
            .from(h)
            .innerJoin(sea).on(sea.houseBl.houseBlId.eq(h.houseBlId))
            .where(h.masterBlId.eq(masterBlId))
            .orderBy(h.createdAt.desc())
            .fetch();
    }

    @Override
    public List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlAirJpaEntity air = QHouseBlAirJpaEntity.houseBlAirJpaEntity;

        return queryFactory
            .select(Projections.constructor(ConsoledHouseBlAirSummary.class,
                h.houseBlId,
                h.hblNo,
                h.shipperCode,
                h.consigneeCode,
                h.docPartnerCode,
                h.pkgQty,
                h.pkgUnit,
                h.grossWeightKg,
                h.cbm,
                air.chargeWeightKg
            ))
            .from(h)
            .innerJoin(air).on(air.houseBl.houseBlId.eq(h.houseBlId))
            .where(h.masterBlId.eq(masterBlId))
            .orderBy(h.createdAt.desc())
            .fetch();
    }
}
