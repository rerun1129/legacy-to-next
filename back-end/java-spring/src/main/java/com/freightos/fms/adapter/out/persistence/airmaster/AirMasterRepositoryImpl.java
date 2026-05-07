package com.freightos.fms.adapter.out.persistence.airmaster;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlAirJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.freightos.common.util.Nullables;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AirMasterRepositoryImpl implements AirMasterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<AirMasterSummary> searchAirMasterSummaries(AirMasterFilter filter, PageRequest pageRequest) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;
        QMasterBlAirJpaEntity air = QMasterBlAirJpaEntity.masterBlAirJpaEntity;

        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;

        List<AirMasterSummary> content = queryFactory
            .select(Projections.constructor(AirMasterSummary.class,
                m.masterBlId,
                m.bound.stringValue(),
                m.mblNo,
                m.shipmentType.stringValue(),
                m.etd,
                m.eta,
                m.grossWeightKg,
                air.chargeWeightKg,
                m.pkgQty,
                m.pkgUnit,
                JPAExpressions.select(h.count()).from(h).where(h.masterBlId.eq(m.masterBlId)),
                m.polCode,
                m.podCode,
                m.shipperCode,
                m.consigneeCode,
                m.notifyCode,
                m.settlePartnerCode,
                air.airlineCode,
                m.masterRefNo,
                m.freightTerm.stringValue(),
                m.operatorCode,
                m.teamCode
            ))
            .from(m)
            .leftJoin(air).on(air.masterBl.masterBlId.eq(m.masterBlId))
            // Air Master B/L 전용 endpoint이므로 jobDiv=AIR 하드코딩
            .where(
                m.jobDiv.eq(MasterBlJobDiv.AIR),
                m.bound.eq(filter.bound()),
                dateBetween(m, filter),
                masterAwbContains(m, filter),
                eqParty(m, filter),
                eqString(air.airlineCode, filter.airlineCode()),
                eqPort(m, filter),
                Nullables.mapOrNull(filter.shipmentType(), t -> m.shipmentType.eq(t)),
                eqString(m.teamCode, filter.teamCode())
            )
            .orderBy(m.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(m.count())
            .from(m)
            .leftJoin(air).on(air.masterBl.masterBlId.eq(m.masterBlId))
            // Air Master B/L 전용 endpoint이므로 jobDiv=AIR 하드코딩
            .where(
                m.jobDiv.eq(MasterBlJobDiv.AIR),
                m.bound.eq(filter.bound()),
                dateBetween(m, filter),
                masterAwbContains(m, filter),
                eqParty(m, filter),
                eqString(air.airlineCode, filter.airlineCode()),
                eqPort(m, filter),
                Nullables.mapOrNull(filter.shipmentType(), t -> m.shipmentType.eq(t)),
                eqString(m.teamCode, filter.teamCode())
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

    private static BooleanExpression dateBetween(QMasterBlJpaEntity m, AirMasterFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? m.eta : m.etd;
        BooleanExpression e = null;
        if (StringUtils.hasText(from)) e = col.goe(from);
        if (StringUtils.hasText(to))   e = (e == null) ? col.loe(to) : e.and(col.loe(to));
        return e;
    }

    private static BooleanExpression masterAwbContains(QMasterBlJpaEntity m, AirMasterFilter filter) {
        if (!StringUtils.hasText(filter.masterAwbValue())) return null;
        // REF 지정 시 masterRefNo, 기본값(MBL 포함)은 mblNo
        return "REF".equals(filter.masterAwbKind())
            ? containsIgnoreCase(m.masterRefNo, filter.masterAwbValue())
            : containsIgnoreCase(m.mblNo, filter.masterAwbValue());
    }

    private static BooleanExpression eqParty(QMasterBlJpaEntity m, AirMasterFilter filter) {
        String code = filter.partyCode();
        PartyKind kind = filter.partyKind();
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return m.shipperCode.eq(code).or(m.consigneeCode.eq(code)).or(m.notifyCode.eq(code));
        }
        StringPath col = switch (kind) {
            case SHIPPER        -> m.shipperCode;
            case CONSIGNEE      -> m.consigneeCode;
            case NOTIFY         -> m.notifyCode;
            case SETTLE_PARTNER -> m.settlePartnerCode;
        };
        return col.eq(code);
    }

    private static BooleanExpression eqPort(QMasterBlJpaEntity m, AirMasterFilter filter) {
        String code = filter.portCode();
        PortKind kind = filter.portKind();
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return m.polCode.eq(code).or(m.podCode.eq(code));
        }
        StringPath col = switch (kind) {
            case POL -> m.polCode;
            case POD -> m.podCode;
        };
        return col.eq(code);
    }
}
