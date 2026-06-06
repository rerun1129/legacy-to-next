package com.freightos.pms.adapter.out.persistence.codename;

import com.freightos.pms.adapter.out.persistence.codename.entity.QPmsAdminUserRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.codename.entity.QPmsCarrierRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.codename.entity.QPmsCustomerRefJpaEntity;
import com.freightos.pms.adapter.out.persistence.codename.entity.QPmsTeamRefJpaEntity;
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
 * admin 스키마 code → name 일괄 조회 전용 QueryDSL 컴포넌트.
 * cross-schema 접근은 이 컴포넌트에만 격리됨.
 */
@Repository
@RequiredArgsConstructor
public class PmsCodeNameQueryRepository {

    private final JPAQueryFactory queryFactory;

    Map<String, String> fetchCustomerNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        QPmsCustomerRefJpaEntity c = QPmsCustomerRefJpaEntity.pmsCustomerRefJpaEntity;
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

    Map<String, String> fetchCarrierNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        QPmsCarrierRefJpaEntity c = QPmsCarrierRefJpaEntity.pmsCarrierRefJpaEntity;
        BooleanExpression inCodes = c.carrierCode.in(codes);
        BooleanExpression notDeleted = c.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(c.carrierCode, c.name)
            .from(c)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(c.carrierCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(c.carrierCode),
                t -> t.get(c.name) != null ? t.get(c.name) : ""
            ));
    }

    Map<String, String> fetchTeamNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        QPmsTeamRefJpaEntity t = QPmsTeamRefJpaEntity.pmsTeamRefJpaEntity;

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
        if (usernames == null || usernames.isEmpty()) return Collections.emptyMap();
        QPmsAdminUserRefJpaEntity u = QPmsAdminUserRefJpaEntity.pmsAdminUserRefJpaEntity;

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
}
