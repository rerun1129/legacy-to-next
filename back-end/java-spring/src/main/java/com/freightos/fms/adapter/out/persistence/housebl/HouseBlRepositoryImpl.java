package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlAirJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.nonbl.entity.QHouseBlNonBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlSeaJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.querydsl.core.types.dsl.StringPath;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.freightos.common.util.Nullables;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HouseBlRepositoryImpl implements HouseBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<HouseBlSummary> searchSummaries(HouseBlFilter filter, PageRequest pageRequest) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlNonBlJpaEntity nonBl = QHouseBlNonBlJpaEntity.houseBlNonBlJpaEntity;

        List<HouseBlSummary> content = queryFactory
            .select(Projections.constructor(HouseBlSummary.class,
                h.houseBlId,
                h.hblNo,
                h.jobDiv.stringValue(),
                h.bound.stringValue(),
                h.polCode,
                h.podCode,
                h.etd,
                h.eta,
                h.shipperCode,
                h.consigneeCode,
                h.pkgQty,
                h.pkgUnit,
                h.createdAt,
                h.notifyCode,
                h.settlePartnerCode,
                h.actualCustomerCode,
                h.grossWeightKg,
                h.cbm,
                nonBl.vesselName,
                nonBl.voyageNo,
                nonBl.linerCode,
                nonBl.linerName
            ))
            .from(h)
            .leftJoin(nonBl).on(nonBl.houseBl.houseBlId.eq(h.houseBlId))
            .where(
                h.jobDiv.eq(filter.jobDiv()),
                Nullables.mapOrNull(filter.bound(), t -> h.bound.eq(t)),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                containsIgnoreCase(h.mblNo, filter.mblNo()),
                containsIgnoreCase(h.shipperCode, filter.shipperCode()),
                containsIgnoreCase(h.consigneeCode, filter.consigneeCode()),
                eqString(h.polCode, filter.polCode()),
                eqString(h.podCode, filter.podCode()),
                dateBetween(h, filter.dateKind(), filter.etdFrom(), filter.etdTo()),
                containsIgnoreCase(nonBl.vesselName, filter.vessel()),
                containsIgnoreCase(nonBl.voyageNo, filter.voyage()),
                containsIgnoreCase(nonBl.linerCode, filter.linerCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                eqString(h.teamCode, filter.teamCode()),
                eqParty(h, filter.partyCode(), filter.partyKind()),
                eqPort(h, filter.portCode(), filter.portKind())
            )
            .orderBy(h.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(h.count())
            .from(h)
            .leftJoin(nonBl).on(nonBl.houseBl.houseBlId.eq(h.houseBlId))
            .where(
                h.jobDiv.eq(filter.jobDiv()),
                Nullables.mapOrNull(filter.bound(), t -> h.bound.eq(t)),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                containsIgnoreCase(h.mblNo, filter.mblNo()),
                containsIgnoreCase(h.shipperCode, filter.shipperCode()),
                containsIgnoreCase(h.consigneeCode, filter.consigneeCode()),
                eqString(h.polCode, filter.polCode()),
                eqString(h.podCode, filter.podCode()),
                dateBetween(h, filter.dateKind(), filter.etdFrom(), filter.etdTo()),
                containsIgnoreCase(nonBl.vesselName, filter.vessel()),
                containsIgnoreCase(nonBl.voyageNo, filter.voyage()),
                containsIgnoreCase(nonBl.linerCode, filter.linerCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                eqString(h.teamCode, filter.teamCode()),
                eqParty(h, filter.partyCode(), filter.partyKind()),
                eqPort(h, filter.portCode(), filter.portKind())
            )
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

    private static BooleanExpression containsIgnoreCase(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::containsIgnoreCase);
    }

    private static BooleanExpression eqString(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::eq);
    }

    private static BooleanExpression dateBetween(
            QHouseBlJpaEntity h, DateKind kind, String from, String to) {
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (kind == DateKind.ETA) ? h.eta : h.etd;
        BooleanExpression e = null;
        if (StringUtils.hasText(from)) e = col.goe(from);
        if (StringUtils.hasText(to))   e = (e == null) ? col.loe(to) : e.and(col.loe(to));
        return e;
    }

    private static BooleanExpression eqParty(QHouseBlJpaEntity h, String code, PartyKind kind) {
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return h.shipperCode.eq(code).or(h.consigneeCode.eq(code)).or(h.notifyCode.eq(code));
        }
        StringPath col = switch (kind) {
            case SHIPPER        -> h.shipperCode;
            case CONSIGNEE      -> h.consigneeCode;
            case NOTIFY         -> h.notifyCode;
            case SETTLE_PARTNER -> h.settlePartnerCode;
        };
        return col.eq(code);
    }

    private static BooleanExpression eqPort(QHouseBlJpaEntity h, String code, PortKind kind) {
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return h.polCode.eq(code).or(h.podCode.eq(code));
        }
        StringPath col = switch (kind) {
            case POL -> h.polCode;
            case POD -> h.podCode;
        };
        return col.eq(code);
    }
}
