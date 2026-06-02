package com.freightos.bms.adapter.out.persistence.codename;

import com.freightos.bms.adapter.out.persistence.codename.entity.QCustomerRefJpaEntity;
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
}
