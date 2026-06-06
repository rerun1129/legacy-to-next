package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlAirRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlNonBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlSeaRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.entity.QPmsHouseBlTruckRefJpaEntity;
import com.freightos.pms.application.pms.projection.PmsCargoRow;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * House B/L ID 목록 기반 화물 수치 일괄 조회 QueryDSL 레포지토리.
 * house_bl LEFT JOIN 확장 테이블(sea/air/truck/non_bl) → fan-out 없는 1:1 조인.
 * MASTER B/L 행은 호출 측에서 제외하고 houseBlIds만 전달.
 */
@Repository
@RequiredArgsConstructor
public class PmsCargoLookupQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PmsCargoRow> fetchByHouseBlIds(List<Long> houseBlIds) {
        if (houseBlIds == null || houseBlIds.isEmpty()) return Collections.emptyList();

        QPmsHouseBlRefJpaEntity bl = QPmsHouseBlRefJpaEntity.pmsHouseBlRefJpaEntity;
        QPmsHouseBlSeaRefJpaEntity sea = QPmsHouseBlSeaRefJpaEntity.pmsHouseBlSeaRefJpaEntity;
        QPmsHouseBlAirRefJpaEntity air = QPmsHouseBlAirRefJpaEntity.pmsHouseBlAirRefJpaEntity;
        QPmsHouseBlTruckRefJpaEntity truck = QPmsHouseBlTruckRefJpaEntity.pmsHouseBlTruckRefJpaEntity;
        QPmsHouseBlNonBlRefJpaEntity nonBl = QPmsHouseBlNonBlRefJpaEntity.pmsHouseBlNonBlRefJpaEntity;

        List<Tuple> rows = queryFactory
            .select(
                bl.houseBlId,
                bl.pkgQty, bl.cbm, bl.grossWeightKg,
                sea.loadType, air.chargeWeightKg,
                truck.chargeWeightKg, truck.loadType,
                nonBl.rton
            )
            .from(bl)
            .leftJoin(sea).on(sea.houseBlId.eq(bl.houseBlId))
            .leftJoin(air).on(air.houseBlId.eq(bl.houseBlId))
            .leftJoin(truck).on(truck.houseBlId.eq(bl.houseBlId))
            .leftJoin(nonBl).on(nonBl.houseBlId.eq(bl.houseBlId))
            .where(bl.houseBlId.in(houseBlIds))
            .fetch();

        return rows.stream()
            .filter(t -> t.get(bl.houseBlId) != null)
            .map(t -> new PmsCargoRow(
                t.get(bl.houseBlId),
                t.get(bl.pkgQty),
                t.get(bl.cbm),
                t.get(bl.grossWeightKg),
                t.get(sea.loadType),
                t.get(air.chargeWeightKg),
                t.get(truck.chargeWeightKg),
                t.get(truck.loadType),
                t.get(nonBl.rton)
            ))
            .toList();
    }
}
