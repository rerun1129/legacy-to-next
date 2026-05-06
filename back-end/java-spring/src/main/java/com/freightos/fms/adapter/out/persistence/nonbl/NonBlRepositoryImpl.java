package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.fms.adapter.out.persistence.housebl.entity.QHouseBlNonBlJpaEntity; // Q-class: 첫 compileJava 후 생성됨
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NonBlRepositoryImpl implements NonBlRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PagedResult<NonBlSummary> searchNonBlSummaries(NonBlFilter filter, PageRequest pageRequest) {
        QHouseBlJpaEntity h = QHouseBlJpaEntity.houseBlJpaEntity;
        QHouseBlNonBlJpaEntity nonBl = QHouseBlNonBlJpaEntity.houseBlNonBlJpaEntity;

        BooleanBuilder where = new BooleanBuilder();
        // Non B/L 전용 endpoint이므로 jobDiv=NON_BL 하드코딩
        where.and(h.jobDiv.eq(JobDiv.NON_BL));
        if (filter.bound() != null) {
            where.and(h.bound.eq(filter.bound()));
        }

        if (StringUtils.hasText(filter.hblNo())) {
            where.and(h.hblNo.containsIgnoreCase(filter.hblNo()));
        }
        if (StringUtils.hasText(filter.etdFrom()) || StringUtils.hasText(filter.etdTo())) {
            StringPath datePath = filter.dateKind() == DateKind.ETA ? h.eta : h.etd;
            if (StringUtils.hasText(filter.etdFrom())) where.and(datePath.goe(filter.etdFrom()));
            if (StringUtils.hasText(filter.etdTo()))   where.and(datePath.loe(filter.etdTo()));
        }
        if (StringUtils.hasText(filter.vessel())) {
            where.and(nonBl.vesselName.containsIgnoreCase(filter.vessel()));
        }
        if (StringUtils.hasText(filter.voyage())) {
            where.and(nonBl.voyageNo.containsIgnoreCase(filter.voyage()));
        }
        if (StringUtils.hasText(filter.linerCode())) {
            where.and(nonBl.linerCode.containsIgnoreCase(filter.linerCode()));
        }
        if (StringUtils.hasText(filter.operatorCode())) {
            where.and(h.operatorCode.eq(filter.operatorCode()));
        }
        if (StringUtils.hasText(filter.teamCode())) {
            where.and(h.teamCode.eq(filter.teamCode()));
        }
        if (StringUtils.hasText(filter.partyCode())) {
            if (filter.partyKind() == null) {
                where.and(h.shipperCode.eq(filter.partyCode())
                        .or(h.consigneeCode.eq(filter.partyCode()))
                        .or(h.notifyCode.eq(filter.partyCode())));
            } else {
                StringPath col = switch (filter.partyKind()) {
                    case SHIPPER -> h.shipperCode;
                    case CONSIGNEE -> h.consigneeCode;
                    case NOTIFY -> h.notifyCode;
                    case SETTLE_PARTNER -> h.settlePartnerCode;
                };
                where.and(col.eq(filter.partyCode()));
            }
        }
        if (StringUtils.hasText(filter.portCode())) {
            if (filter.portKind() == null) {
                where.and(h.polCode.eq(filter.portCode()).or(h.podCode.eq(filter.portCode())));
            } else {
                StringPath col = switch (filter.portKind()) {
                    case POL -> h.polCode;
                    case POD -> h.podCode;
                };
                where.and(col.eq(filter.portCode()));
            }
        }

        List<NonBlSummary> content = queryFactory
            .select(Projections.constructor(NonBlSummary.class,
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
            .innerJoin(nonBl).on(nonBl.houseBl.houseBlId.eq(h.houseBlId))
            .where(where)
            .orderBy(h.createdAt.desc())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .limit(pageRequest.getSize())
            .fetch();

        long total = queryFactory
            .select(h.count())
            .from(h)
            .innerJoin(nonBl).on(nonBl.houseBl.houseBlId.eq(h.houseBlId))
            .where(where)
            .fetchOne();

        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return PagedResult.of(content, total, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }
}
