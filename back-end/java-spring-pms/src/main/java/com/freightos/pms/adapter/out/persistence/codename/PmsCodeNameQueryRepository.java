package com.freightos.pms.adapter.out.persistence.codename;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * admin 스키마 code → name 일괄 조회 전용 JDBC 컴포넌트.
 * cross-schema 접근은 이 컴포넌트에만 격리됨.
 *
 * = ANY(?) 단일 배열 파라미터로 IN-explosion 없이 compact SQL 로그를 유지한다.
 */
@Repository
@RequiredArgsConstructor
public class PmsCodeNameQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_CUSTOMER =
        "SELECT customer_code, name FROM admin.customer WHERE customer_code = ANY(?) AND deleted_at IS NULL";

    private static final String SQL_CARRIER =
        "SELECT carrier_code, name FROM admin.carrier WHERE carrier_code = ANY(?) AND deleted_at IS NULL";

    private static final String SQL_TEAM =
        "SELECT team_code, name FROM admin.team WHERE team_code = ANY(?)";

    private static final String SQL_OPERATOR =
        "SELECT username, user_eng_name FROM admin.admin_user WHERE username = ANY(?)";

    Map<String, String> fetchCustomerNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        return fetchCodeNameMap(SQL_CUSTOMER, codes, "customer_code", "name");
    }

    Map<String, String> fetchCarrierNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        return fetchCodeNameMap(SQL_CARRIER, codes, "carrier_code", "name");
    }

    Map<String, String> fetchTeamNames(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        return fetchCodeNameMap(SQL_TEAM, codes, "team_code", "name");
    }

    Map<String, String> fetchOperatorNames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) return Collections.emptyMap();
        return fetchCodeNameMap(SQL_OPERATOR, usernames, "username", "user_eng_name");
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    /**
     * text[] 배열 파라미터로 code → name 맵을 조회한다. null name은 빈 문자열로 변환.
     */
    private Map<String, String> fetchCodeNameMap(
            String sql, Collection<String> keys, String keyCol, String nameCol) {

        String[] keyArray = keys.toArray(String[]::new);
        Map<String, String> result = new HashMap<>();
        jdbcTemplate.query(
            con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setArray(1, con.createArrayOf("text", keyArray));
                return ps;
            },
            rs -> {
                String key = rs.getString(keyCol);
                if (key != null) {
                    String name = rs.getString(nameCol);
                    result.put(key, name != null ? name : "");
                }
            }
        );
        return result;
    }
}
