package com.freightos.fms.adapter.out.persistence.blquicksearch;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.QMasterBlJpaEntity;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.common.util.Nullables;
import com.querydsl.core.types.dsl.Expressions;
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
public class BlQuickSearchRepositoryImpl implements BlQuickSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BlQuickSearchSummary> searchHouse(BlQuickSearchFilter filter, int limit) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;

        return queryFactory
                .select(Projections.constructor(BlQuickSearchSummary.class,
                        h.houseBlId,
                        Expressions.constant("HOUSE"),
                        h.hblNo,
                        h.jobDiv.stringValue(),
                        h.bound.stringValue(),
                        h.shipperCode,
                        h.polCode,
                        h.podCode,
                        h.etd))
                .from(h)
                .where(
                        Nullables.mapOrNull(filter.jobDiv(), h.jobDiv::eq),
                        Nullables.mapOrNull(filter.bound(), h.bound::eq),
                        houseBlDateBetween(h, filter),
                        containsIgnoreCase(h.hblNo, filter.blNo()),
                        eqString(h.teamCode, filter.teamCode()),
                        eqString(h.operatorCode, filter.operatorCode()),
                        eqString(h.salesManCode, filter.salesManCode()),
                        eqString(h.polCode, filter.polCode()),
                        eqString(h.podCode, filter.podCode()),
                        eqHouseParty(h, filter))
                .orderBy(h.hblNo.asc(), h.houseBlId.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<BlQuickSearchSummary> searchMaster(BlQuickSearchFilter filter, int limit) {
        QMasterBlJpaEntity m = QMasterBlJpaEntity.masterBlJpaEntity;

        // House JobDiv → MasterBlJobDiv 변환 (SEA/AIR만 도달, null이면 술어 생략)
        MasterBlJobDiv masterJobDiv = filter.jobDiv() == null
                ? null
                : MasterBlJobDiv.fromCode(filter.jobDiv().name());

        return queryFactory
                .select(Projections.constructor(BlQuickSearchSummary.class,
                        m.masterBlId,
                        Expressions.constant("MASTER"),
                        m.mblNo,
                        m.jobDiv.stringValue(),
                        m.bound.stringValue(),
                        m.shipperCode,
                        m.polCode,
                        m.podCode,
                        m.etd))
                .from(m)
                .where(
                        Nullables.mapOrNull(masterJobDiv, m.jobDiv::eq),
                        Nullables.mapOrNull(filter.bound(), m.bound::eq),
                        masterBlDateBetween(m, filter),
                        containsIgnoreCase(m.mblNo, filter.blNo()),
                        eqString(m.teamCode, filter.teamCode()),
                        eqString(m.operatorCode, filter.operatorCode()),
                        eqString(m.polCode, filter.polCode()),
                        eqString(m.podCode, filter.podCode()),
                        eqMasterParty(m, filter))
                .orderBy(m.mblNo.asc(), m.masterBlId.asc())
                .limit(limit)
                .fetch();
    }

    // ── 공통 헬퍼 ────────────────────────────────────────────────────

    private static BooleanExpression containsIgnoreCase(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::containsIgnoreCase);
    }

    private static BooleanExpression eqString(StringPath col, String v) {
        return Nullables.mapIfHasText(v, col::eq);
    }

    // ── House 전용 헬퍼 ──────────────────────────────────────────────

    private static BooleanExpression houseBlDateBetween(QHouseBlJpaEntity h, BlQuickSearchFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? h.eta : h.etd;
        BooleanExpression expr = null;
        if (StringUtils.hasText(from)) expr = col.goe(from);
        if (StringUtils.hasText(to)) expr = (expr == null) ? col.loe(to) : expr.and(col.loe(to));
        return expr;
    }

    private static BooleanExpression eqHouseParty(QHouseBlJpaEntity h, BlQuickSearchFilter filter) {
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

    // ── Master 전용 헬퍼 ─────────────────────────────────────────────

    private static BooleanExpression masterBlDateBetween(QMasterBlJpaEntity m, BlQuickSearchFilter filter) {
        String from = filter.dateFrom();
        String to = filter.dateTo();
        if (!StringUtils.hasText(from) && !StringUtils.hasText(to)) return null;
        StringPath col = (filter.dateKind() == DateKind.ETA) ? m.eta : m.etd;
        BooleanExpression expr = null;
        if (StringUtils.hasText(from)) expr = col.goe(from);
        if (StringUtils.hasText(to)) expr = (expr == null) ? col.loe(to) : expr.and(col.loe(to));
        return expr;
    }

    private static BooleanExpression eqMasterParty(QMasterBlJpaEntity m, BlQuickSearchFilter filter) {
        String code = filter.partyCode();
        PartyKind kind = filter.partyKind();
        if (!StringUtils.hasText(code)) return null;
        if (kind == null) {
            return m.shipperCode.eq(code).or(m.consigneeCode.eq(code)).or(m.notifyCode.eq(code));
        }
        // Master는 docPartnerCode 없음 — SETTLE_PARTNER만 지원
        StringPath col = switch (kind) {
            case SHIPPER        -> m.shipperCode;
            case CONSIGNEE      -> m.consigneeCode;
            case NOTIFY         -> m.notifyCode;
            case SETTLE_PARTNER -> m.settlePartnerCode;
        };
        return col.eq(code);
    }
}
