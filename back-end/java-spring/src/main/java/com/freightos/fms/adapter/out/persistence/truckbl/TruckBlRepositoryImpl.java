package com.freightos.fms.adapter.out.persistence.truckbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlTruckJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.freightos.common.util.Nullables;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TruckBlRepositoryImpl implements TruckBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<TruckBlSummary> searchTruckBlSummaries(TruckBlFilter filter, PageRequest pageRequest) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlTruckJpaEntity truck = QHouseBlTruckJpaEntity.houseBlTruckJpaEntity;

        List<TruckBlSummary> content = queryFactory
            .select(Projections.constructor(TruckBlSummary.class,
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
                h.notifyCode,
                h.docPartnerCode,
                truck.truckerCode,
                h.pkgQty,
                h.pkgUnit,
                h.grossWeightKg,
                h.cbm,
                h.createdAt
            ))
            .from(h)
            .leftJoin(truck).on(truck.houseBl.houseBlId.eq(h.houseBlId))
            // Truck B/L 전용 endpoint이므로 jobDiv=TRUCK 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.TRUCK),
                Nullables.mapOrNull(filter.bound(), t -> h.bound.eq(t)),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                containsIgnoreCase(truck.truckerCode, filter.truckerCode()),
                containsIgnoreCase(h.docPartnerCode, filter.docPartnerCode()),
                dateBetween(h, filter.dateKind(), filter.etdFrom(), filter.etdTo()),
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
            .leftJoin(truck).on(truck.houseBl.houseBlId.eq(h.houseBlId))
            // Truck B/L 전용 endpoint이므로 jobDiv=TRUCK 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.TRUCK),
                Nullables.mapOrNull(filter.bound(), t -> h.bound.eq(t)),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                containsIgnoreCase(truck.truckerCode, filter.truckerCode()),
                containsIgnoreCase(h.docPartnerCode, filter.docPartnerCode()),
                dateBetween(h, filter.dateKind(), filter.etdFrom(), filter.etdTo()),
                eqString(h.operatorCode, filter.operatorCode()),
                eqString(h.teamCode, filter.teamCode()),
                eqParty(h, filter.partyCode(), filter.partyKind()),
                eqPort(h, filter.portCode(), filter.portKind())
            )
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
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

    private static BooleanExpression eqParty(
            QHouseBlJpaEntity h, String code, PartyKind kind) {
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

    private static BooleanExpression eqPort(
            QHouseBlJpaEntity h, String code, PortKind kind) {
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
