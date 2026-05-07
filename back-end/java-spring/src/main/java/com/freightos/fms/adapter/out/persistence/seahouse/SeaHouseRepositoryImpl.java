package com.freightos.fms.adapter.out.persistence.seahouse;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlSeaJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlContainerJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.domain.seahouse.PartnerKind;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SeaHouseRepositoryImpl implements SeaHouseRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<SeaHouseSummary> searchSeaHouseSummaries(SeaHouseFilter filter, PageRequest pageRequest) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlSeaJpaEntity sea = QHouseBlSeaJpaEntity.houseBlSeaJpaEntity;
        QHouseBlContainerJpaEntity c = QHouseBlContainerJpaEntity.houseBlContainerJpaEntity;

        List<SeaHouseSummary> content = queryFactory
            .select(Projections.constructor(SeaHouseSummary.class,
                h.houseBlId,
                h.hblNo,
                h.bound.stringValue(),
                h.mblNo,
                h.shipmentType.stringValue(),
                h.etd,
                h.eta,
                h.grossWeightKg,
                sea.rton,
                h.pkgQty,
                h.pkgUnit,
                h.polCode,
                h.podCode,
                h.shipperCode,
                h.consigneeCode,
                h.notifyCode,
                h.settlePartnerCode,
                h.docPartnerCode,
                sea.linerCode,
                h.masterRefNo,
                h.freightTerm.stringValue(),
                h.incoterms.stringValue(),
                h.actualCustomerCode,
                h.salesManCode,
                h.teamCode,
                sea.loadType.stringValue(),
                h.cbm,
                sea.deliveryCode,
                sea.vesselName,
                sea.voyageNo,
                JPAExpressions.select(c.count())
                    .from(c)
                    .where(c.houseBlId.eq(h.houseBlId), c.lengthFeet.eq(20)),
                JPAExpressions.select(c.count())
                    .from(c)
                    .where(c.houseBlId.eq(h.houseBlId), c.lengthFeet.eq(40)),
                JPAExpressions.select(c.lengthFeet.sum().castToNum(Long.class))
                    .from(c)
                    .where(c.houseBlId.eq(h.houseBlId))
            ))
            .from(h)
            .leftJoin(sea).on(sea.houseBl.houseBlId.eq(h.houseBlId))
            // Sea House B/L 전용 endpoint이므로 jobDiv=SEA 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.SEA),
                h.bound.eq(filter.bound()),
                dateBetween(h, filter),
                masterBlContains(h, filter),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                eqParty(h, filter),
                eqString(h.actualCustomerCode, filter.actualCustomerCode()),
                eqPartner(h, filter),
                eqString(sea.linerCode, filter.linerCode()),
                eqPort(h, filter),
                containsIgnoreCase(sea.vesselName, filter.vesselName()),
                containsIgnoreCase(sea.voyageNo, filter.voyageNo()),
                filter.shipmentType() != null ? h.shipmentType.eq(filter.shipmentType()) : null,
                eqString(h.teamCode, filter.teamCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                filter.salesClass() != null ? h.salesClass.eq(filter.salesClass()) : null,
                eqString(h.salesManCode, filter.salesManCode()),
                filter.incoterms() != null ? h.incoterms.eq(filter.incoterms()) : null,
                filter.loadType() != null ? sea.loadType.eq(filter.loadType()) : null
            )
            .orderBy(h.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(h.count())
            .from(h)
            .leftJoin(sea).on(sea.houseBl.houseBlId.eq(h.houseBlId))
            // Sea House B/L 전용 endpoint이므로 jobDiv=SEA 하드코딩
            .where(
                h.jobDiv.eq(JobDiv.SEA),
                h.bound.eq(filter.bound()),
                dateBetween(h, filter),
                masterBlContains(h, filter),
                containsIgnoreCase(h.hblNo, filter.hblNo()),
                eqParty(h, filter),
                eqString(h.actualCustomerCode, filter.actualCustomerCode()),
                eqPartner(h, filter),
                eqString(sea.linerCode, filter.linerCode()),
                eqPort(h, filter),
                containsIgnoreCase(sea.vesselName, filter.vesselName()),
                containsIgnoreCase(sea.voyageNo, filter.voyageNo()),
                filter.shipmentType() != null ? h.shipmentType.eq(filter.shipmentType()) : null,
                eqString(h.teamCode, filter.teamCode()),
                eqString(h.operatorCode, filter.operatorCode()),
                filter.salesClass() != null ? h.salesClass.eq(filter.salesClass()) : null,
                eqString(h.salesManCode, filter.salesManCode()),
                filter.incoterms() != null ? h.incoterms.eq(filter.incoterms()) : null,
                filter.loadType() != null ? sea.loadType.eq(filter.loadType()) : null
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

    private static BooleanExpression dateBetween(QHouseBlJpaEntity h, SeaHouseFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? h.eta : h.etd;
        BooleanExpression e = null;
        if (StringUtils.hasText(from)) e = col.goe(from);
        if (StringUtils.hasText(to))   e = (e == null) ? col.loe(to) : e.and(col.loe(to));
        return e;
    }

    private static BooleanExpression masterBlContains(QHouseBlJpaEntity h, SeaHouseFilter filter) {
        if (!StringUtils.hasText(filter.masterBlValue())) return null;
        // REF 지정 시 masterRefNo, 기본값(MBL 포함)은 mblNo
        return "REF".equals(filter.masterBlKind())
            ? containsIgnoreCase(h.masterRefNo, filter.masterBlValue())
            : containsIgnoreCase(h.mblNo, filter.masterBlValue());
    }

    private static BooleanExpression eqParty(QHouseBlJpaEntity h, SeaHouseFilter filter) {
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

    private static BooleanExpression eqPort(QHouseBlJpaEntity h, SeaHouseFilter filter) {
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

    // Settle/Doc Partner 라벨 드롭 (kind=null이면 두 컬럼 OR)
    private static BooleanExpression eqPartner(QHouseBlJpaEntity h, SeaHouseFilter filter) {
        String code = filter.partnerCode();
        PartnerKind kind = filter.partnerKind();
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return h.settlePartnerCode.eq(code).or(h.docPartnerCode.eq(code));
        }
        StringPath col = switch (kind) {
            case SETTLE_PARTNER -> h.settlePartnerCode;
            case DOC_PARTNER    -> h.docPartnerCode;
        };
        return col.eq(code);
    }

}
