package com.freightos.fms.adapter.out.persistence.airhouse;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlAirJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AirHouseRepositoryImpl implements AirHouseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<AirHouseSummary> searchAirHouseSummaries(AirHouseFilter filter, PageRequest pageRequest) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlAirJpaEntity air = QHouseBlAirJpaEntity.houseBlAirJpaEntity;

        List<AirHouseSummary> content = queryFactory
            .select(Projections.constructor(AirHouseSummary.class,
                h.houseBlId,
                h.hblNo,
                h.bound.stringValue(),
                h.mblNo,
                h.shipmentType.stringValue(),
                h.etd,
                h.eta,
                h.grossWeightKg,
                air.chargeWeightKg,
                h.pkgQty,
                h.pkgUnit,
                h.polCode,
                h.podCode,
                h.shipperCode,
                h.consigneeCode,
                h.notifyCode,
                h.settlePartnerCode,
                h.docPartnerCode,
                air.airlineCode,
                h.masterRefNo,
                h.freightTerm.stringValue(),
                h.incoterms.stringValue(),
                h.actualCustomerCode,
                h.salesManCode,
                h.teamCode
            ))
            .from(h)
            .leftJoin(air).on(air.houseBl.houseBlId.eq(h.houseBlId))
            // Air House B/L 전용 endpoint이므로 jobDiv=AIR 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.AIR),
                h.bound.eq(filter.bound()),
                dateBetween(h, filter),
                masterAwbContains(h, filter),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                eqParty(h, filter),
                eqString(h.actualCustomerCode, filter.actualCustomerCode()),
                eqString(h.settlePartnerCode, filter.settlePartnerCode()),
                eqString(air.airlineCode, filter.airlineCode()),
                eqPort(h, filter),
                filter.shipmentType() != null ? h.shipmentType.eq(filter.shipmentType()) : null,
                eqString(h.teamCode, filter.teamCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                filter.salesClass() != null ? h.salesClass.eq(filter.salesClass()) : null,
                eqString(h.salesManCode, filter.salesManCode()),
                filter.incoterms() != null ? h.incoterms.eq(filter.incoterms()) : null
            )
            .orderBy(h.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(h.count())
            .from(h)
            .leftJoin(air).on(air.houseBl.houseBlId.eq(h.houseBlId))
            // Air House B/L 전용 endpoint이므로 jobDiv=AIR 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.AIR),
                h.bound.eq(filter.bound()),
                dateBetween(h, filter),
                masterAwbContains(h, filter),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                eqParty(h, filter),
                eqString(h.actualCustomerCode, filter.actualCustomerCode()),
                eqString(h.settlePartnerCode, filter.settlePartnerCode()),
                eqString(air.airlineCode, filter.airlineCode()),
                eqPort(h, filter),
                filter.shipmentType() != null ? h.shipmentType.eq(filter.shipmentType()) : null,
                eqString(h.teamCode, filter.teamCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                filter.salesClass() != null ? h.salesClass.eq(filter.salesClass()) : null,
                eqString(h.salesManCode, filter.salesManCode()),
                filter.incoterms() != null ? h.incoterms.eq(filter.incoterms()) : null
            )
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }

    private static BooleanExpression containsIgnoreCase(StringPath col, String v) {
        return StringUtils.hasText(v) ? col.containsIgnoreCase(v) : null;
    }

    private static BooleanExpression eqString(StringPath col, String v) {
        return StringUtils.hasText(v) ? col.eq(v) : null;
    }

    private static BooleanExpression dateBetween(QHouseBlJpaEntity h, AirHouseFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? h.eta : h.etd;
        BooleanExpression e = null;
        if (StringUtils.hasText(from)) e = col.goe(from);
        if (StringUtils.hasText(to))   e = (e == null) ? col.loe(to) : e.and(col.loe(to));
        return e;
    }

    private static BooleanExpression masterAwbContains(QHouseBlJpaEntity h, AirHouseFilter filter) {
        if (!StringUtils.hasText(filter.masterAwbValue())) return null;
        // REF 지정 시 masterRefNo, 기본값(MBL 포함)은 mblNo
        return "REF".equals(filter.masterAwbKind())
            ? containsIgnoreCase(h.masterRefNo, filter.masterAwbValue())
            : containsIgnoreCase(h.mblNo, filter.masterAwbValue());
    }

    private static BooleanExpression eqParty(QHouseBlJpaEntity h, AirHouseFilter filter) {
        String code = filter.partyCode();
        PartyKind kind = filter.partyKind();
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

    private static BooleanExpression eqPort(QHouseBlJpaEntity h, AirHouseFilter filter) {
        String code = filter.portCode();
        PortKind kind = filter.portKind();
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
