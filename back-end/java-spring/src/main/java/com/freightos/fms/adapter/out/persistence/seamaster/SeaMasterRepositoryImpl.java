package com.freightos.fms.adapter.out.persistence.seamaster;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlSeaJpaEntity;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;
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
public class SeaMasterRepositoryImpl implements SeaMasterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<SeaMasterSummary> searchSeaMasterSummaries(SeaMasterFilter filter, PageRequest pageRequest) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;
        QMasterBlSeaJpaEntity sea = QMasterBlSeaJpaEntity.masterBlSeaJpaEntity;
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;

        List<SeaMasterSummary> content = queryFactory
            .select(Projections.constructor(SeaMasterSummary.class,
                m.masterBlId,
                m.bound.stringValue(),
                m.mblNo,
                m.shipmentType.stringValue(),
                m.etd,
                m.eta,
                m.grossWeightKg,
                sea.rton,
                m.pkgQty,
                m.pkgUnit,
                JPAExpressions.select(h.count()).from(h).where(h.masterBlId.eq(m.masterBlId)),
                m.polCode,
                m.podCode,
                m.shipperCode,
                m.consigneeCode,
                m.notifyCode,
                m.settlePartnerCode,
                sea.linerCode,
                m.masterRefNo,
                m.freightTerm.stringValue(),
                m.operatorCode,
                m.teamCode,
                sea.vesselName,
                sea.voyageNo,
                sea.loadType.stringValue(),
                m.cbm
            ))
            .from(m)
            .leftJoin(sea).on(sea.masterBl.masterBlId.eq(m.masterBlId))
            // Sea Master B/L 전용 endpoint이므로 jobDiv=SEA 하드코딩
            .where(
                m.jobDiv.eq(MasterBlJobDiv.SEA),
                m.bound.eq(filter.bound()),
                dateBetween(m, filter),
                masterBlContains(m, filter),
                eqParty(m, filter),
                eqString(sea.linerCode, filter.linerCode()),
                eqPort(m, filter),
                containsIgnoreCase(sea.vesselName, filter.vesselName()),
                containsIgnoreCase(sea.voyageNo, filter.voyageNo()),
                Nullables.mapOrNull(filter.loadType(), t -> sea.loadType.eq(t)),
                Nullables.mapOrNull(filter.shipmentType(), t -> m.shipmentType.eq(t))
            )
            .orderBy(m.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(m.count())
            .from(m)
            .leftJoin(sea).on(sea.masterBl.masterBlId.eq(m.masterBlId))
            // Sea Master B/L 전용 endpoint이므로 jobDiv=SEA 하드코딩
            .where(
                m.jobDiv.eq(MasterBlJobDiv.SEA),
                m.bound.eq(filter.bound()),
                dateBetween(m, filter),
                masterBlContains(m, filter),
                eqParty(m, filter),
                eqString(sea.linerCode, filter.linerCode()),
                eqPort(m, filter),
                containsIgnoreCase(sea.vesselName, filter.vesselName()),
                containsIgnoreCase(sea.voyageNo, filter.voyageNo()),
                Nullables.mapOrNull(filter.loadType(), t -> sea.loadType.eq(t)),
                Nullables.mapOrNull(filter.shipmentType(), t -> m.shipmentType.eq(t))
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

    private static BooleanExpression dateBetween(QMasterBlJpaEntity m, SeaMasterFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? m.eta : m.etd;
        BooleanExpression e = null;
        if (StringUtils.hasText(from)) e = col.goe(from);
        if (StringUtils.hasText(to))   e = (e == null) ? col.loe(to) : e.and(col.loe(to));
        return e;
    }

    private static BooleanExpression masterBlContains(QMasterBlJpaEntity m, SeaMasterFilter filter) {
        if (!StringUtils.hasText(filter.masterBlValue())) return null;
        // REF 지정 시 masterRefNo, 기본값(MBL 포함)은 mblNo
        return "REF".equals(filter.masterBlKind())
            ? containsIgnoreCase(m.masterRefNo, filter.masterBlValue())
            : containsIgnoreCase(m.mblNo, filter.masterBlValue());
    }

    private static BooleanExpression eqParty(QMasterBlJpaEntity m, SeaMasterFilter filter) {
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

    private static BooleanExpression eqPort(QMasterBlJpaEntity m, SeaMasterFilter filter) {
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
