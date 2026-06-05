package com.freightos.bms.adapter.out.persistence.codename;

import com.freightos.bms.adapter.out.persistence.codename.entity.QAdminUserRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QCustomerRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QFreightRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QHouseBlRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QMasterBlRefJpaEntity;
import com.freightos.bms.adapter.out.persistence.codename.entity.QTeamRefJpaEntity;
import com.freightos.bms.application.port.out.BlDerived;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * admin·fms 스키마 code → name 일괄 조회 전용 QueryDSL 컴포넌트.
 * cross-schema 접근은 이 컴포넌트에만 격리됨.
 * Q클래스는 빌드 시 APT가 자동 생성한다.
 */
@Repository
@RequiredArgsConstructor
public class CodeNameQueryRepository {

    private final JPAQueryFactory queryFactory;

    Map<String, String> fetchCustomerNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QCustomerRefJpaEntity c = QCustomerRefJpaEntity.customerRefJpaEntity;
        BooleanExpression inCodes = c.customerCode.in(codes);
        BooleanExpression notDeleted = c.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(c.customerCode, c.name)
            .from(c)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(c.customerCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(c.customerCode),
                t -> t.get(c.name) != null ? t.get(c.name) : ""
            ));
    }

    Map<String, String> fetchTeamNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QTeamRefJpaEntity t = QTeamRefJpaEntity.teamRefJpaEntity;

        List<Tuple> rows = queryFactory
            .select(t.teamCode, t.name)
            .from(t)
            .where(t.teamCode.in(codes))
            .fetch();

        return rows.stream()
            .filter(row -> row.get(t.teamCode) != null)
            .collect(Collectors.toMap(
                row -> row.get(t.teamCode),
                row -> row.get(t.name) != null ? row.get(t.name) : ""
            ));
    }

    Map<String, String> fetchOperatorNames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyMap();
        }
        QAdminUserRefJpaEntity u = QAdminUserRefJpaEntity.adminUserRefJpaEntity;

        List<Tuple> rows = queryFactory
            .select(u.username, u.userEngName)
            .from(u)
            .where(u.username.in(usernames))
            .fetch();

        return rows.stream()
            .filter(row -> row.get(u.username) != null)
            .collect(Collectors.toMap(
                row -> row.get(u.username),
                row -> row.get(u.userEngName) != null ? row.get(u.userEngName) : ""
            ));
    }

    /**
     * freight_code → name 일괄 조회.
     * admin.freight deleted_at IS NULL 활성 항목만 포함.
     */
    Map<String, String> fetchFreightNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QFreightRefJpaEntity f = QFreightRefJpaEntity.freightRefJpaEntity;
        BooleanExpression inCodes = f.freightCode.in(codes);
        BooleanExpression notDeleted = f.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(f.freightCode, f.name)
            .from(f)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(f.freightCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(f.freightCode),
                t -> t.get(f.name) != null ? t.get(f.name) : ""
            ));
    }

    /**
     * HOUSE B/L ID 목록 → 파생 정보(jobDiv·bound·hblNo·etd·eta).
     * bl_id는 VARCHAR, house_bl_id는 BIGINT이므로 stringValue() 비교.
     */
    Map<String, BlDerived> fetchHouseBlDerived(Collection<String> blIds) {
        if (blIds == null || blIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QHouseBlRefJpaEntity h = QHouseBlRefJpaEntity.houseBlRefJpaEntity;

        List<Tuple> rows = queryFactory
            .select(h.houseBlId, h.jobDiv, h.bound, h.hblNo, h.etd, h.eta)
            .from(h)
            .where(h.houseBlId.stringValue().in(blIds))
            .fetch();

        return rows.stream()
            .filter(row -> row.get(h.houseBlId) != null)
            .collect(Collectors.toMap(
                row -> String.valueOf(row.get(h.houseBlId)),
                row -> new BlDerived(
                    row.get(h.jobDiv),
                    row.get(h.bound),
                    row.get(h.hblNo),
                    row.get(h.etd),
                    row.get(h.eta)
                )
            ));
    }

    /**
     * MASTER B/L ID 목록 → 파생 정보(jobDiv·bound·mblNo·etd·eta).
     * bl_id는 VARCHAR, master_bl_id는 BIGINT이므로 stringValue() 비교.
     */
    Map<String, BlDerived> fetchMasterBlDerived(Collection<String> blIds) {
        if (blIds == null || blIds.isEmpty()) {
            return Collections.emptyMap();
        }
        QMasterBlRefJpaEntity m = QMasterBlRefJpaEntity.masterBlRefJpaEntity;

        List<Tuple> rows = queryFactory
            .select(m.masterBlId, m.jobDiv, m.bound, m.mblNo, m.etd, m.eta)
            .from(m)
            .where(m.masterBlId.stringValue().in(blIds))
            .fetch();

        return rows.stream()
            .filter(row -> row.get(m.masterBlId) != null)
            .collect(Collectors.toMap(
                row -> String.valueOf(row.get(m.masterBlId)),
                row -> new BlDerived(
                    row.get(m.jobDiv),
                    row.get(m.bound),
                    row.get(m.mblNo),
                    row.get(m.etd),
                    row.get(m.eta)
                )
            ));
    }
}
