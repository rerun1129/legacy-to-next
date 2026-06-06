package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.projection.PmsCargoRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

/**
 * House B/L ID 목록 기반 cargo 수치 + house 식별 정보 일괄 조회 레포지토리.
 * house_bl LEFT JOIN 확장 테이블(sea/air/truck/non_bl) → fan-out 없는 1:1 조인.
 *
 * = ANY(?) 단일 배열 파라미터로 IN-explosion 없이 compact SQL 로그를 유지한다.
 */
@Repository
@RequiredArgsConstructor
public class PmsCargoLookupQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL = """
        SELECT b.house_bl_id, b.pkg_qty, b.cbm, b.gross_weight_kg,
               s.load_type  AS sea_load_type,
               a.charge_weight_kg AS air_cw,
               t.charge_weight_kg AS truck_cw, t.load_type AS truck_load_type,
               n.rton,
               b.hbl_no, b.mbl_no, b.job_div, b.bound,
               b.etd, b.eta, b.pol_code, b.pod_code,
               b.sales_man_code, b.incoterms, b.team_code
        FROM fms.house_bl b
        LEFT JOIN fms.house_bl_sea   s ON s.house_bl_id = b.house_bl_id
        LEFT JOIN fms.house_bl_air   a ON a.house_bl_id = b.house_bl_id
        LEFT JOIN fms.house_bl_truck t ON t.house_bl_id = b.house_bl_id
        LEFT JOIN fms.house_bl_non_bl n ON n.house_bl_id = b.house_bl_id
        WHERE b.house_bl_id = ANY(?)
        """;

    public List<PmsCargoRow> fetchByHouseBlIds(List<Long> houseBlIds) {
        if (houseBlIds == null || houseBlIds.isEmpty()) return Collections.emptyList();

        return jdbcTemplate.query(
            con -> {
                PreparedStatement ps = con.prepareStatement(SQL);
                ps.setArray(1, con.createArrayOf("bigint", houseBlIds.toArray()));
                return ps;
            },
            (rs, ignored) -> new PmsCargoRow(
                rs.getLong("house_bl_id"),
                rs.getObject("pkg_qty", Integer.class),
                rs.getBigDecimal("cbm"),
                rs.getBigDecimal("gross_weight_kg"),
                rs.getString("sea_load_type"),
                rs.getBigDecimal("air_cw"),
                rs.getBigDecimal("truck_cw"),
                rs.getString("truck_load_type"),
                rs.getBigDecimal("rton"),
                rs.getString("hbl_no"),
                rs.getString("mbl_no"),
                rs.getString("job_div"),
                rs.getString("bound"),
                rs.getString("etd"),
                rs.getString("eta"),
                rs.getString("pol_code"),
                rs.getString("pod_code"),
                rs.getString("sales_man_code"),
                rs.getString("incoterms"),
                rs.getString("team_code")
            )
        );
    }
}
