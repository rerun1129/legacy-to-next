package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.projection.PmsMasterDetailRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;

/**
 * Master B/L ID 목록 기반 식별 정보 일괄 조회 레포지토리.
 * Phase-2 keyed lookup: 집계 쿼리에서 누락된 master 식별 정보(mblNo/jobDiv 등)를 보완한다.
 *
 * = ANY(?) 단일 배열 파라미터로 IN-explosion 없이 compact SQL 로그를 유지한다.
 */
@Repository
@RequiredArgsConstructor
public class PmsMasterLookupQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL = """
        SELECT master_bl_id, mbl_no, job_div, bound, etd, eta, pol_code, pod_code
        FROM fms.master_bl
        WHERE master_bl_id = ANY(?)
        """;

    public List<PmsMasterDetailRow> fetchByMasterBlIds(List<Long> masterBlIds) {
        if (masterBlIds == null || masterBlIds.isEmpty()) return Collections.emptyList();

        return jdbcTemplate.query(
            con -> {
                PreparedStatement ps = con.prepareStatement(SQL);
                ps.setArray(1, con.createArrayOf("bigint", masterBlIds.toArray()));
                return ps;
            },
            (rs, ignored) -> new PmsMasterDetailRow(
                rs.getLong("master_bl_id"),
                rs.getString("mbl_no"),
                rs.getString("job_div"),
                rs.getString("bound"),
                rs.getString("etd"),
                rs.getString("eta"),
                rs.getString("pol_code"),
                rs.getString("pod_code")
            )
        );
    }
}
