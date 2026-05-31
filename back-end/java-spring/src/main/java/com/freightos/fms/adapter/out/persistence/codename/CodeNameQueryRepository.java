package com.freightos.fms.adapter.out.persistence.codename;

import com.freightos.fms.adapter.out.persistence.codename.entity.QAdminUserRefJpaEntity;
import com.freightos.fms.adapter.out.persistence.codename.entity.QCarrierRefJpaEntity;
import com.freightos.fms.adapter.out.persistence.codename.entity.QCustomerRefJpaEntity;
import com.freightos.fms.adapter.out.persistence.codename.entity.QHsCodeRefJpaEntity;
import com.freightos.fms.adapter.out.persistence.codename.entity.QPortRefJpaEntity;
import com.freightos.fms.adapter.out.persistence.codename.entity.QTeamRefJpaEntity;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
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

    Map<String, String> fetchPortNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QPortRefJpaEntity p = QPortRefJpaEntity.portRefJpaEntity;
        BooleanExpression inCodes = p.portCode.in(codes);
        BooleanExpression notDeleted = p.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(p.portCode, p.name)
            .from(p)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(p.portCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(p.portCode),
                t -> t.get(p.name) != null ? t.get(p.name) : ""
            ));
    }

    Map<String, String> fetchCarrierNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QCarrierRefJpaEntity ca = QCarrierRefJpaEntity.carrierRefJpaEntity;
        BooleanExpression inCodes = ca.carrierCode.in(codes);
        BooleanExpression notDeleted = ca.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(ca.carrierCode, ca.name)
            .from(ca)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(ca.carrierCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(ca.carrierCode),
                t -> t.get(ca.name) != null ? t.get(ca.name) : ""
            ));
    }

    /**
     * username → COALESCE(user_eng_name, email) 일괄 조회.
     * 두 컬럼 모두 null인 행은 맵에서 제외(autocomplete와 동일 정책).
     */
    Map<String, String> fetchUserNames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyMap();
        }
        QAdminUserRefJpaEntity u = QAdminUserRefJpaEntity.adminUserRefJpaEntity;
        BooleanExpression inUsernames = u.username.in(usernames);
        BooleanExpression notDeleted = u.deletedAt.isNull();
        StringExpression displayName = Expressions.stringTemplate("coalesce({0}, {1})", u.userEngName, u.email);

        List<Tuple> rows = queryFactory
            .select(u.username, displayName)
            .from(u)
            .where(inUsernames, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(u.username) != null && t.get(displayName) != null)
            .collect(Collectors.toMap(
                t -> t.get(u.username),
                t -> t.get(displayName)
            ));
    }

    Map<String, String> fetchHsCodeNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QHsCodeRefJpaEntity h = QHsCodeRefJpaEntity.hsCodeRefJpaEntity;
        BooleanExpression inCodes = h.hsCode.in(codes);
        BooleanExpression notDeleted = h.deletedAt.isNull();

        List<Tuple> rows = queryFactory
            .select(h.hsCode, h.name)
            .from(h)
            .where(inCodes, notDeleted)
            .fetch();

        return rows.stream()
            .filter(t -> t.get(h.hsCode) != null)
            .collect(Collectors.toMap(
                t -> t.get(h.hsCode),
                t -> t.get(h.name) != null ? t.get(h.name) : ""
            ));
    }

    /**
     * team_code → name 일괄 조회.
     * admin.team은 deleted_at이 없고 active BOOLEAN으로 활성 여부를 관리하므로
     * deletedAt.isNull() 대신 active.isTrue() 필터를 사용한다.
     */
    Map<String, String> fetchTeamNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        QTeamRefJpaEntity t = QTeamRefJpaEntity.teamRefJpaEntity;
        BooleanExpression inCodes = t.teamCode.in(codes);
        BooleanExpression isActive = t.active.isTrue();

        List<Tuple> rows = queryFactory
            .select(t.teamCode, t.name)
            .from(t)
            .where(inCodes, isActive)
            .fetch();

        return rows.stream()
            .filter(row -> row.get(t.teamCode) != null)
            .collect(Collectors.toMap(
                row -> row.get(t.teamCode),
                row -> row.get(t.name) != null ? row.get(t.name) : ""
            ));
    }
}
